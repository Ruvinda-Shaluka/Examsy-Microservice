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
        return null; // Next commit
    }

    @Transactional(readOnly = true)
    @Override
    public VaultExamsResponseDTO getVaultExams(String studentUsername, Integer classId) {
        return null; // Next commit
    }

    @Transactional
    @Override
    public ProctoringStatsDTO logProctoringEvent(Integer examId, String studentUsername, ProctoringLogDTO logDTO) {
        return null; // Next commit
    }

    @Transactional
    @Override
    public void logSecurityViolation(String studentUsername, ProctoringDTO dto) {
        // Next commit
    }

    @Transactional(readOnly = true)
    @Override
    public StudentAnalyticsDTO getStudentAnalytics(String studentUsername) {
        return null; // Next commit
    }
}
