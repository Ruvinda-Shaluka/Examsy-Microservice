package lk.ijse.examsy.examservice.service.impl;

import lk.ijse.examsy.examservice.dto.*;
import lk.ijse.examsy.examservice.entity.*;
import lk.ijse.examsy.examservice.event.ExamSubmittedEvent;
import lk.ijse.examsy.examservice.kafka.ExamEventProducer;
import lk.ijse.examsy.examservice.repository.*;
import lk.ijse.examsy.examservice.service.StudentExamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentExamServiceImpl implements StudentExamService {

    private final ExamRepo examRepository;
    private final QuestionRepo questionRepository;
    private final QuestionOptionRepo questionOptionRepository;
    private final ExamSubmissionRepo examSubmissionRepository;
    private final SubmissionAnswerRepo submissionAnswerRepository;
    private final ProctoringLogRepo proctoringLogRepository;
    private final ExamEventProducer examEventProducer;

    @Transactional
    @Override
    public StudentExamViewDTO getExamForStudent(String studentUsername, Integer examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        LocalDateTime now = LocalDateTime.now();
        if (exam.getScheduledStartTime() != null && now.isBefore(exam.getScheduledStartTime())) {
            throw new IllegalStateException("Exam has not started yet.");
        }
        if (exam.getDeadlineTime() != null && now.isAfter(exam.getDeadlineTime())) {
            throw new IllegalStateException("Exam deadline has already passed.");
        }

        // Retrieve or initialize the student's submission record
        ExamSubmission submission = examSubmissionRepository.findByExamIdAndStudentUsername(examId, studentUsername)
                .orElseGet(() -> {
                    ExamSubmission newSub = ExamSubmission.builder()
                            .exam(exam)
                            .studentUsername(studentUsername)
                            .studentName(studentUsername)
                            .actualStartTime(LocalDateTime.now())
                            .status("IN_PROGRESS")
                            .proctoringStatus("SECURE")
                            .suspiciousEventCount(0)
                            .totalTimeAwaySeconds(0)
                            .lastKnownAction("EXAM_STARTED")
                            .build();
                    return examSubmissionRepository.save(newSub);
                });

        if ("SUBMITTED".equals(submission.getStatus())) {
            throw new IllegalStateException("You have already submitted this exam.");
        }

        List<Question> questions = questionRepository.findByExamIdOrderByOrderIndexAsc(examId);

        List<StudentQuestionDTO> questionDTOs = questions.stream().map(q -> {
            List<QuestionOption> options = questionOptionRepository.findByQuestionId(q.getId());
            List<StudentOptionDTO> optionDTOs = options.stream().map(opt ->
                    StudentOptionDTO.builder()
                            .id(opt.getId())
                            .optionText(opt.getOptionText())
                            .build()
            ).collect(Collectors.toList());

            return StudentQuestionDTO.builder()
                    .id(q.getId())
                    .questionText(q.getQuestionText())
                    .questionType(q.getQuestionType())
                    .points(q.getPoints())
                    .orderIndex(q.getOrderIndex())
                    .options(optionDTOs)
                    .build();
        }).collect(Collectors.toList());

        return StudentExamViewDTO.builder()
                .examId(exam.getId())
                .title(exam.getTitle())
                .examType(exam.getExamType())
                .examMode(exam.getExamMode())
                .durationMinutes(exam.getDurationMinutes())
                .deadlineTime(exam.getDeadlineTime())
                .pdfResourceUrl(exam.getPdfResourceUrl())
                .submissionId(submission.getId())
                .submissionStatus(submission.getStatus())
                .questions(questionDTOs)
                .build();
    }

    @Transactional
    @Override
    public ExamResultDTO submitExam(String studentUsername, Integer examId, ExamSubmitDTO dto) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        ExamSubmission submission = examSubmissionRepository.findByExamIdAndStudentUsername(examId, studentUsername)
                .orElseThrow(() -> new RuntimeException("No active attempt found for this exam"));

        if ("SUBMITTED".equals(submission.getStatus())) {
            throw new IllegalStateException("Exam is already submitted.");
        }

        BigDecimal calculatedScore = BigDecimal.ZERO;
        List<Question> questions = questionRepository.findByExamIdOrderByOrderIndexAsc(examId);
        Map<Integer, Question> questionMap = questions.stream().collect(Collectors.toMap(Question::getId, q -> q));

        if (dto.getAnswers() != null) {
            for (AnswerSubmitDTO aDto : dto.getAnswers()) {
                Question question = questionMap.get(aDto.getQuestionId());
                if (question == null) continue;

                BigDecimal pointsAwarded = BigDecimal.ZERO;

                if ("MCQ".equalsIgnoreCase(question.getQuestionType()) && aDto.getSelectedOptionId() != null) {
                    List<QuestionOption> options = questionOptionRepository.findByQuestionId(question.getId());
                    boolean correct = options.stream().anyMatch(opt ->
                            opt.getId().equals(aDto.getSelectedOptionId()) && Boolean.TRUE.equals(opt.getIsCorrect()));
                    if (correct) {
                        pointsAwarded = question.getPoints() != null ? question.getPoints() : BigDecimal.ZERO;
                        calculatedScore = calculatedScore.add(pointsAwarded);
                    }
                }

                SubmissionAnswer answer = SubmissionAnswer.builder()
                        .submission(submission)
                        .question(question)
                        .selectedOptionId(aDto.getSelectedOptionId())
                        .answerText(aDto.getTextAnswer())
                        .scoreAwarded(pointsAwarded)
                        .build();

                submissionAnswerRepository.save(answer);
            }
        }

        submission.setStatus("SUBMITTED");
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setCalculatedScore(calculatedScore);
        if (dto.getPdfSubmissionUrl() != null) {
            submission.setPdfSubmissionUrl(dto.getPdfSubmissionUrl());
        }

        // Calculate Grade Letter based on max score
        String grade = "F";
        if (exam.getMaxScore() != null && exam.getMaxScore().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal percentage = calculatedScore.divide(exam.getMaxScore(), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
            if (percentage.compareTo(BigDecimal.valueOf(85)) >= 0) grade = "A+";
            else if (percentage.compareTo(BigDecimal.valueOf(75)) >= 0) grade = "A";
            else if (percentage.compareTo(BigDecimal.valueOf(65)) >= 0) grade = "B";
            else if (percentage.compareTo(BigDecimal.valueOf(50)) >= 0) grade = "C";
            else if (percentage.compareTo(BigDecimal.valueOf(40)) >= 0) grade = "D";
        }
        submission.setAwardedGradeLetter(grade);
        ExamSubmission savedSubmission = examSubmissionRepository.save(submission);

        // Event-driven decoupling: Publish event to Kafka for Phase 9 AI Grading Service
        ExamSubmittedEvent event = ExamSubmittedEvent.builder()
                .submissionId(savedSubmission.getId())
                .examId(exam.getId())
                .studentId(savedSubmission.getStudentId())
                .studentUsername(studentUsername)
                .examType(exam.getExamType())
                .pdfSubmissionUrl(savedSubmission.getPdfSubmissionUrl())
                .submittedAt(savedSubmission.getSubmittedAt())
                .build();

        examEventProducer.publishExamSubmitted(event);
        log.info("Exam submission completed for student '{}' on exam ID {}. Score: {}",
                studentUsername, examId, calculatedScore);

        return ExamResultDTO.builder()
                .submissionId(savedSubmission.getId())
                .examId(exam.getId())
                .status("SUBMITTED")
                .calculatedScore(calculatedScore)
                .maxScore(exam.getMaxScore())
                .awardedGradeLetter(grade)
                .submittedAt(savedSubmission.getSubmittedAt())
                .message("Exam submitted successfully.")
                .build();
    }

    @Transactional(readOnly = true)
    @Override
    public VaultExamsResponseDTO getVaultExams(String studentUsername, Integer classId) {
        List<Exam> exams = examRepository.findByCourseId(classId);

        List<VaultExamItemDTO> completedExams = new ArrayList<>();
        for (Exam exam : exams) {
            Optional<ExamSubmission> optSub = examSubmissionRepository.findByExamIdAndStudentUsername(exam.getId(), studentUsername);
            if (optSub.isPresent()) {
                ExamSubmission sub = optSub.get();
                if ("SUBMITTED".equals(sub.getStatus())) {
                    BigDecimal score = sub.getFinalScore() != null ? sub.getFinalScore() :
                            (sub.getCalculatedScore() != null ? sub.getCalculatedScore() : BigDecimal.ZERO);

                    completedExams.add(VaultExamItemDTO.builder()
                            .examId(exam.getId())
                            .title(exam.getTitle())
                            .examType(exam.getExamType())
                            .score(score)
                            .maxScore(exam.getMaxScore())
                            .gradeLetter(sub.getAwardedGradeLetter())
                            .completedAt(sub.getSubmittedAt())
                            .feedback(sub.getPdfFeedback())
                            .build());
                }
            }
        }

        return VaultExamsResponseDTO.builder()
                .classId(classId)
                .completedExams(completedExams)
                .build();
    }

    @Transactional(readOnly = true)
    @Override
    public StudentAnalyticsDTO getStudentAnalytics(String studentUsername) {
        List<ExamSubmission> submissions = examSubmissionRepository.findByStudentUsername(studentUsername);
        List<ExamSubmission> completed = submissions.stream()
                .filter(s -> "SUBMITTED".equals(s.getStatus()))
                .collect(Collectors.toList());

        if (completed.isEmpty()) {
            return StudentAnalyticsDTO.builder()
                    .totalExamsTaken(0)
                    .averageScorePercentage(BigDecimal.ZERO)
                    .totalHonorsReceived(0)
                    .suspiciousEventsCount(0)
                    .integrityScore(BigDecimal.valueOf(100))
                    .build();
        }

        BigDecimal totalPercentage = BigDecimal.ZERO;
        int honorsCount = 0;
        int totalSuspicious = 0;

        for (ExamSubmission sub : completed) {
            BigDecimal score = sub.getFinalScore() != null ? sub.getFinalScore() :
                    (sub.getCalculatedScore() != null ? sub.getCalculatedScore() : BigDecimal.ZERO);
            BigDecimal max = sub.getExam().getMaxScore() != null ? sub.getExam().getMaxScore() : BigDecimal.valueOf(100);

            if (max.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal pct = score.divide(max, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                totalPercentage = totalPercentage.add(pct);
                if (pct.compareTo(BigDecimal.valueOf(80)) >= 0) {
                    honorsCount++;
                }
            }

            if (sub.getSuspiciousEventCount() != null) {
                totalSuspicious += sub.getSuspiciousEventCount();
            }
        }

        BigDecimal avgPct = totalPercentage.divide(BigDecimal.valueOf(completed.size()), 2, RoundingMode.HALF_UP);
        BigDecimal penalty = BigDecimal.valueOf(totalSuspicious * 5L);
        BigDecimal integrity = BigDecimal.valueOf(100).subtract(penalty);
        if (integrity.compareTo(BigDecimal.ZERO) < 0) integrity = BigDecimal.ZERO;

        return StudentAnalyticsDTO.builder()
                .totalExamsTaken(completed.size())
                .averageScorePercentage(avgPct)
                .totalHonorsReceived(honorsCount)
                .suspiciousEventsCount(totalSuspicious)
                .integrityScore(integrity)
                .build();
    }

    @Transactional
    @Override
    public ProctoringStatsDTO logProctoringEvent(Integer examId, String studentUsername, ProctoringLogDTO logDTO) {
        ExamSubmission submission = examSubmissionRepository.findByExamIdAndStudentUsername(examId, studentUsername)
                .orElseThrow(() -> new RuntimeException("No active attempt found for this exam"));

        ProctoringLog logEntry = ProctoringLog.builder()
                .examSubmission(submission)
                .eventType(logDTO.getEventType())
                .durationSeconds(logDTO.getDurationSeconds() != null ? logDTO.getDurationSeconds() : 0)
                .build();

        proctoringLogRepository.save(logEntry);

        // Update telemetry counters
        int suspicious = submission.getSuspiciousEventCount() != null ? submission.getSuspiciousEventCount() + 1 : 1;
        int timeAway = submission.getTotalTimeAwaySeconds() != null ? submission.getTotalTimeAwaySeconds() : 0;
        if (logDTO.getDurationSeconds() != null) {
            timeAway += logDTO.getDurationSeconds();
        }

        submission.setSuspiciousEventCount(suspicious);
        submission.setTotalTimeAwaySeconds(timeAway);
        submission.setLastKnownAction(logDTO.getEventType());

        // Dynamic proctoring severity classification
        if (suspicious >= 5 || timeAway >= 60) {
            submission.setProctoringStatus("SUSPICIOUS");
        } else if (suspicious >= 2 || timeAway >= 20) {
            submission.setProctoringStatus("WARNING");
        } else {
            submission.setProctoringStatus("SECURE");
        }

        ExamSubmission updated = examSubmissionRepository.save(submission);
        log.warn("Proctoring event '{}' recorded for student '{}' on exam ID {}. Status: {}",
                logDTO.getEventType(), studentUsername, examId, updated.getProctoringStatus());

        return ProctoringStatsDTO.builder()
                .submissionId(updated.getId())
                .suspiciousEvents(updated.getSuspiciousEventCount())
                .totalTimeAwaySeconds(updated.getTotalTimeAwaySeconds())
                .proctoringStatus(updated.getProctoringStatus())
                .build();
    }

    @Transactional
    @Override
    public void logSecurityViolation(String studentUsername, ProctoringDTO dto) {
        ExamSubmission submission = examSubmissionRepository.findByExamIdAndStudentUsername(dto.getExamId(), studentUsername)
                .orElseThrow(() -> new RuntimeException("No active attempt found for this exam"));

        ProctoringLog logEntry = ProctoringLog.builder()
                .examSubmission(submission)
                .eventType(dto.getViolationType())
                .durationSeconds(dto.getDurationSeconds() != null ? dto.getDurationSeconds() : 0)
                .build();

        proctoringLogRepository.save(logEntry);

        int suspicious = submission.getSuspiciousEventCount() != null ? submission.getSuspiciousEventCount() + 1 : 1;
        submission.setSuspiciousEventCount(suspicious);
        submission.setLastKnownAction("VIOLATION: " + dto.getViolationType());
        if (suspicious >= 3) {
            submission.setProctoringStatus("WARNING");
        }
        examSubmissionRepository.save(submission);
        log.warn("Security violation '{}' logged silently for student '{}' on exam ID {}",
                dto.getViolationType(), studentUsername, dto.getExamId());
    }
}

