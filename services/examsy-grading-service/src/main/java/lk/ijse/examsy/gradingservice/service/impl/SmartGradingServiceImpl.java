package lk.ijse.examsy.gradingservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lk.ijse.examsy.gradingservice.dto.PendingGradingDTO;
import lk.ijse.examsy.gradingservice.entity.GradingTask;
import lk.ijse.examsy.gradingservice.event.ExamSubmittedEvent;
import lk.ijse.examsy.gradingservice.event.GradeReleasedEvent;
import lk.ijse.examsy.gradingservice.kafka.GradeEventProducer;
import lk.ijse.examsy.gradingservice.repository.GradingTaskRepo;
import lk.ijse.examsy.gradingservice.service.GroqGradingService;
import lk.ijse.examsy.gradingservice.service.OCRService;
import lk.ijse.examsy.gradingservice.service.SmartGradingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmartGradingServiceImpl implements SmartGradingService {

    private final GradingTaskRepo gradingTaskRepo;
    private final OCRService ocrService;
    private final GroqGradingService groqGradingService;
    private final GradeEventProducer gradeEventProducer;
    private final ObjectMapper objectMapper;

    @Transactional
    @Override
    public Map<String, Object> autoGradeSubmission(Integer examId, Integer submissionId) {
        log.info("On-demand auto-grading requested for examId: {}, submissionId: {}", examId, submissionId);

        GradingTask task = gradingTaskRepo.findBySubmissionId(submissionId)
                .orElseGet(() -> GradingTask.builder()
                        .submissionId(submissionId)
                        .examId(examId)
                        .studentUsername("student")
                        .status("PENDING_OCR")
                        .build());

        if (task.getPdfSubmissionUrl() == null || task.getPdfSubmissionUrl().isBlank()) {
            throw new IllegalStateException("No PDF document submission found for submission ID: " + submissionId);
        }

        // 1. OCR Extraction if not already cached
        String ocrText = task.getExtractedOcrText();
        if (ocrText == null || ocrText.isBlank()) {
            ocrText = ocrService.extractTextFromPdfUrl(task.getPdfSubmissionUrl());
            task.setExtractedOcrText(ocrText);
        }

        // 2. Groq LLM Evaluation
        String rubricPrompt = "Exam ID: " + examId + " Rubric and evaluation criteria.";
        String modelAnswers = "Model answers for Exam ID " + examId;
        Map<String, Object> aiResult = groqGradingService.evaluateAnswer(rubricPrompt, modelAnswers, ocrText);

        // 3. Persist AI findings
        try {
            if (aiResult.get("suggestedScore") != null) {
                task.setSuggestedScore(new BigDecimal(aiResult.get("suggestedScore").toString()));
            }
            if (aiResult.get("matchedConcepts") != null) {
                task.setMatchedConcepts(objectMapper.writeValueAsString(aiResult.get("matchedConcepts")));
            }
            if (aiResult.get("missingConcepts") != null) {
                task.setMissingConcepts(objectMapper.writeValueAsString(aiResult.get("missingConcepts")));
            }
            if (aiResult.get("incorrectParts") != null) {
                task.setIncorrectParts(objectMapper.writeValueAsString(aiResult.get("incorrectParts")));
            }
            if (aiResult.get("comments") != null) {
                task.setComments(aiResult.get("comments").toString());
            }
            if (aiResult.get("confidence") != null) {
                task.setConfidence(aiResult.get("confidence").toString());
            }
            task.setStatus("PENDING_TEACHER_REVIEW");
            gradingTaskRepo.save(task);
        } catch (Exception e) {
            log.error("Error serializing AI grading result: {}", e.getMessage());
        }

        // Attach max score if available (fallback 100)
        aiResult.put("maxScore", task.getMaxScore() != null ? task.getMaxScore() : BigDecimal.valueOf(100));
        return aiResult;
    }

    @Transactional(readOnly = true)
    @Override
    public List<PendingGradingDTO> getPendingPdfGradings(String teacherUsername) {
        log.info("Fetching pending PDF gradings for teacher: {}", teacherUsername);

        List<GradingTask> tasks;
        if (teacherUsername != null && !teacherUsername.isBlank()) {
            tasks = gradingTaskRepo.findByTeacherUsernameOrderByCreatedAtDesc(teacherUsername);
            if (tasks.isEmpty()) {
                tasks = gradingTaskRepo.findByStatus("PENDING_TEACHER_REVIEW");
                if (tasks.isEmpty()) {
                    tasks = gradingTaskRepo.findAll();
                }
            }
        } else {
            tasks = gradingTaskRepo.findAll();
        }

        List<PendingGradingDTO> result = new ArrayList<>();
        for (GradingTask t : tasks) {
            if (t.getPdfSubmissionUrl() != null && !t.getPdfSubmissionUrl().isBlank() && !"APPROVED".equals(t.getStatus())) {
                result.add(PendingGradingDTO.builder()
                        .id(t.getSubmissionId())
                        .examId(t.getExamId())
                        .studentName(t.getStudentUsername())
                        .examTitle("Exam #" + t.getExamId())
                        .status(t.getStatus() != null ? t.getStatus() : "PENDING")
                        .pdfUrl(t.getPdfSubmissionUrl())
                        .build());
            }
        }
        return result;
    }

    @Transactional
    @Override
    public void approveAndReleaseGrade(String teacherUsername, Integer examId, Integer submissionId, BigDecimal score, BigDecimal aiScore, String comments) {
        log.info("Teacher '{}' approving grade for examId: {}, submissionId: {}, score: {}", teacherUsername, examId, submissionId, score);

        GradingTask task = gradingTaskRepo.findBySubmissionId(submissionId)
                .orElseGet(() -> GradingTask.builder()
                        .submissionId(submissionId)
                        .examId(examId)
                        .studentUsername("student")
                        .build());

        task.setFinalScore(score);
        if (aiScore != null) {
            task.setSuggestedScore(aiScore);
        }
        task.setComments(comments);
        task.setTeacherUsername(teacherUsername);
        task.setStatus("APPROVED");

        // Grade letter derivation
        BigDecimal max = task.getMaxScore() != null ? task.getMaxScore() : BigDecimal.valueOf(100);
        String grade = "F";
        if (max.compareTo(BigDecimal.ZERO) > 0 && score != null) {
            BigDecimal pct = score.divide(max, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
            if (pct.compareTo(BigDecimal.valueOf(85)) >= 0) grade = "A+";
            else if (pct.compareTo(BigDecimal.valueOf(75)) >= 0) grade = "A";
            else if (pct.compareTo(BigDecimal.valueOf(65)) >= 0) grade = "B";
            else if (pct.compareTo(BigDecimal.valueOf(50)) >= 0) grade = "C";
            else if (pct.compareTo(BigDecimal.valueOf(40)) >= 0) grade = "D";
        }
        task.setAwardedGradeLetter(grade);
        gradingTaskRepo.save(task);

        // Publish event to Kafka topic 'examsy.grade.released' for Phase 10 Notification Service
        GradeReleasedEvent event = GradeReleasedEvent.builder()
                .submissionId(submissionId)
                .examId(examId)
                .studentId(task.getStudentId())
                .studentUsername(task.getStudentUsername())
                .finalScore(score)
                .maxScore(max)
                .awardedGradeLetter(grade)
                .feedback(comments)
                .releasedAt(LocalDateTime.now())
                .build();

        gradeEventProducer.publishGradeReleased(event);
        log.info("Grade approved and GradeReleasedEvent dispatched for submission ID: {}", submissionId);
    }

    @Async
    @Transactional
    @Override
    public void processKafkaExamSubmission(ExamSubmittedEvent event) {
        log.info("Asynchronously processing Kafka exam submission event for submissionId: {}", event.getSubmissionId());

        Optional<GradingTask> existing = gradingTaskRepo.findBySubmissionId(event.getSubmissionId());
        GradingTask task = existing.orElseGet(() -> GradingTask.builder()
                .submissionId(event.getSubmissionId())
                .examId(event.getExamId())
                .studentId(event.getStudentId())
                .studentUsername(event.getStudentUsername())
                .examType(event.getExamType())
                .pdfSubmissionUrl(event.getPdfSubmissionUrl())
                .submittedAt(event.getSubmittedAt())
                .status("PENDING_OCR")
                .build());

        if (event.getPdfSubmissionUrl() != null && !event.getPdfSubmissionUrl().isBlank()) {
            task.setStatus("IN_PROGRESS");
            gradingTaskRepo.save(task);

            try {
                // Extract handwriting OCR
                String ocrText = ocrService.extractTextFromPdfUrl(event.getPdfSubmissionUrl());
                task.setExtractedOcrText(ocrText);

                // Run Groq AI
                Map<String, Object> aiResult = groqGradingService.evaluateAnswer(
                        "Exam #" + event.getExamId() + " Questions Rubric",
                        "Exam #" + event.getExamId() + " Model Answer Rubric",
                        ocrText
                );

                if (aiResult.get("suggestedScore") != null) {
                    task.setSuggestedScore(new BigDecimal(aiResult.get("suggestedScore").toString()));
                }
                if (aiResult.get("comments") != null) {
                    task.setComments(aiResult.get("comments").toString());
                }
                if (aiResult.get("confidence") != null) {
                    task.setConfidence(aiResult.get("confidence").toString());
                }
                task.setStatus("PENDING_TEACHER_REVIEW");
            } catch (Exception e) {
                log.error("Failed to automatically grade submission {}: {}", event.getSubmissionId(), e.getMessage());
                task.setStatus("PENDING_TEACHER_REVIEW");
                task.setComments("Automatic OCR/AI grading encountered an issue: " + e.getMessage());
            }
        } else {
            task.setStatus("COMPLETED");
        }

        gradingTaskRepo.save(task);
        log.info("Completed background processing for submission ID: {}. Final status: {}",
                event.getSubmissionId(), task.getStatus());
    }
}
