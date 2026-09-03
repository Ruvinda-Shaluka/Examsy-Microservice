package lk.ijse.examsy.examservice.service.impl;

import lk.ijse.examsy.examservice.dto.*;
import lk.ijse.examsy.examservice.entity.*;
import lk.ijse.examsy.examservice.repository.*;
import lk.ijse.examsy.examservice.service.TeacherExamService;
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
public class TeacherExamServiceImpl implements TeacherExamService {

    private final ExamRepo examRepository;
    private final QuestionRepo questionRepository;
    private final QuestionOptionRepo questionOptionRepository;
    private final ExamSubmissionRepo examSubmissionRepository;

    @Transactional
    @Override
    public void publishExam(String teacherUsername, ExamPublishDTO dto) {
        for (Integer classId : dto.getTargetClassIds()) {
            Exam exam = Exam.builder()
                    .courseId(classId)
                    .teacherUsername(teacherUsername)
                    .title(dto.getTitle())
                    .examMode(dto.getExamMode())
                    .examType(dto.getExamType())
                    .scheduledStartTime(dto.getScheduledStartTime())
                    .deadlineTime(dto.getDeadlineTime())
                    .durationMinutes(dto.getDurationMinutes())
                    .pdfResourceUrl(dto.getPdfResourceUrl())
                    .maxScore(dto.getMaxScore())
                    .status("PUBLISHED")
                    .build();

            Exam savedExam = examRepository.save(exam);

            if (dto.getQuestions() != null && !dto.getQuestions().isEmpty()) {
                int order = 1;
                for (QuestionPublishDTO qDto : dto.getQuestions()) {
                    Question question = Question.builder()
                            .exam(savedExam)
                            .questionText(qDto.getQuestionText())
                            .questionType(qDto.getQuestionType())
                            .points(qDto.getPoints())
                            .orderIndex(qDto.getOrderIndex() != null ? qDto.getOrderIndex() : order++)
                            .modelAnswer(qDto.getModelAnswer())
                            .build();

                    Question savedQuestion = questionRepository.save(question);

                    if (qDto.getOptions() != null && !qDto.getOptions().isEmpty()) {
                        List<QuestionOption> options = qDto.getOptions().stream().map(optDto ->
                                QuestionOption.builder()
                                        .question(savedQuestion)
                                        .optionText(optDto.getOptionText())
                                        .isCorrect(optDto.getIsCorrect() != null ? optDto.getIsCorrect() : false)
                                        .build()
                        ).collect(Collectors.toList());

                        questionOptionRepository.saveAll(options);
                    }
                }
            }

            log.info("Exam '{}' (ID {}) published for class ID {} by teacher '{}'",
                    savedExam.getTitle(), savedExam.getId(), classId, teacherUsername);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<ExamSummaryDTO> getClassExams(String teacherUsername, Integer classId) {
        List<Exam> exams = examRepository.findByCourseId(classId);

        return exams.stream().map(exam -> {
            List<ExamSubmission> subs = examSubmissionRepository.findByExamId(exam.getId());
            return ExamSummaryDTO.builder()
                    .id(exam.getId())
                    .title(exam.getTitle())
                    .examType(exam.getExamType())
                    .examMode(exam.getExamMode())
                    .scheduledStartTime(exam.getScheduledStartTime())
                    .deadlineTime(exam.getDeadlineTime())
                    .durationMinutes(exam.getDurationMinutes())
                    .maxScore(exam.getMaxScore())
                    .status(exam.getStatus())
                    .totalSubmissions(subs.size())
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void deleteExam(String teacherUsername, Integer examId) {
        Exam exam = examRepository.findByIdAndTeacherUsername(examId, teacherUsername)
                .orElseThrow(() -> new RuntimeException("Exam not found or unauthorized"));
        examRepository.delete(exam);
        log.info("Exam ID {} deleted by teacher '{}'", examId, teacherUsername);
    }

    @Transactional
    @Override
    public void updateExamTiming(String teacherUsername, Integer examId, UpdateExamDeadlineDTO dto) {
        Exam exam = examRepository.findByIdAndTeacherUsername(examId, teacherUsername)
                .orElseThrow(() -> new RuntimeException("Exam not found or unauthorized"));

        if (dto.getDeadlineTime() != null) {
            exam.setDeadlineTime(dto.getDeadlineTime());
        }
        if (dto.getAdditionalMinutes() != null && dto.getAdditionalMinutes() > 0) {
            exam.setDurationMinutes(exam.getDurationMinutes() + dto.getAdditionalMinutes());
        }

        examRepository.save(exam);
        log.info("Exam ID {} timings updated by teacher '{}'", examId, teacherUsername);
    }

    @Transactional(readOnly = true)
    @Override
    public OngoingExamGroupDTO getOngoingExams(String teacherUsername) {
        List<Exam> exams = examRepository.findByTeacherUsername(teacherUsername);
        LocalDateTime now = LocalDateTime.now();

        List<ExamSummaryDTO> ongoing = new ArrayList<>();
        List<ExamSummaryDTO> upcoming = new ArrayList<>();

        for (Exam exam : exams) {
            ExamSummaryDTO dto = ExamSummaryDTO.builder()
                    .id(exam.getId())
                    .title(exam.getTitle())
                    .examType(exam.getExamType())
                    .examMode(exam.getExamMode())
                    .scheduledStartTime(exam.getScheduledStartTime())
                    .deadlineTime(exam.getDeadlineTime())
                    .durationMinutes(exam.getDurationMinutes())
                    .maxScore(exam.getMaxScore())
                    .status(exam.getStatus())
                    .totalSubmissions(examSubmissionRepository.findByExamId(exam.getId()).size())
                    .build();

            if (exam.getScheduledStartTime() != null && exam.getScheduledStartTime().isAfter(now)) {
                upcoming.add(dto);
            } else if (exam.getDeadlineTime() == null || exam.getDeadlineTime().isAfter(now)) {
                ongoing.add(dto);
            }
        }

        return OngoingExamGroupDTO.builder()
                .ongoingExams(ongoing)
                .upcomingExams(upcoming)
                .build();
    }

    @Transactional(readOnly = true)
    @Override
    public List<LiveStudentMonitorDTO> getLiveMonitorData(Integer examId, String teacherUsername) {
        examRepository.findByIdAndTeacherUsername(examId, teacherUsername)
                .orElseThrow(() -> new RuntimeException("Exam not found or unauthorized"));

        List<ExamSubmission> submissions = examSubmissionRepository.findByExamId(examId);

        return submissions.stream().map(sub -> LiveStudentMonitorDTO.builder()
                .studentId(sub.getStudentId())
                .studentName(sub.getStudentName() != null ? sub.getStudentName() : sub.getStudentUsername())
                .studentUsername(sub.getStudentUsername())
                .submissionStatus(sub.getStatus())
                .proctoringStatus(sub.getProctoringStatus())
                .suspiciousEvents(sub.getSuspiciousEventCount())
                .timeAwaySeconds(sub.getTotalTimeAwaySeconds())
                .lastAction(sub.getLastKnownAction())
                .startedAt(sub.getActualStartTime())
                .build()
        ).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void broadcastToExam(Integer examId, String teacherUsername, String message) {
        examRepository.findByIdAndTeacherUsername(examId, teacherUsername)
                .orElseThrow(() -> new RuntimeException("Exam not found or unauthorized"));

        log.info("Broadcast dispatched to exam ID {} by teacher '{}': {}", examId, teacherUsername, message);
    }

    @Transactional
    @Override
    public void warnStudent(Integer examId, Integer studentId, String teacherUsername, String message) {
        examRepository.findByIdAndTeacherUsername(examId, teacherUsername)
                .orElseThrow(() -> new RuntimeException("Exam not found or unauthorized"));

        log.warn("Teacher '{}' sent warning to student ID {} during exam ID {}: {}",
                teacherUsername, studentId, examId, message);
    }

    @Transactional(readOnly = true)
    @Override
    public ExamAnalyticsDTO getExamAnalytics(Integer examId, String teacherUsername) {
        Exam exam = examRepository.findByIdAndTeacherUsername(examId, teacherUsername)
                .orElseThrow(() -> new RuntimeException("Exam not found or unauthorized"));

        List<ExamSubmission> completedSubs = examSubmissionRepository.findByExamIdAndStatus(examId, "SUBMITTED");

        if (completedSubs.isEmpty()) {
            return ExamAnalyticsDTO.builder()
                    .examId(examId)
                    .title(exam.getTitle())
                    .totalParticipants(0)
                    .averageScore(BigDecimal.ZERO)
                    .highestScore(BigDecimal.ZERO)
                    .lowestScore(BigDecimal.ZERO)
                    .passingRate(BigDecimal.ZERO)
                    .gradeDistribution(Collections.emptyMap())
                    .build();
        }

        BigDecimal total = BigDecimal.ZERO;
        BigDecimal highest = BigDecimal.ZERO;
        BigDecimal lowest = BigDecimal.valueOf(1000);
        int passCount = 0;
        Map<String, Long> gradeDist = new HashMap<>();

        for (ExamSubmission sub : completedSubs) {
            BigDecimal score = sub.getFinalScore() != null ? sub.getFinalScore() :
                    (sub.getCalculatedScore() != null ? sub.getCalculatedScore() : BigDecimal.ZERO);

            total = total.add(score);
            if (score.compareTo(highest) > 0) highest = score;
            if (score.compareTo(lowest) < 0) lowest = score;

            if (exam.getMaxScore() != null && exam.getMaxScore().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal pct = score.divide(exam.getMaxScore(), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                if (pct.compareTo(BigDecimal.valueOf(50)) >= 0) passCount++;
            }

            String grade = sub.getAwardedGradeLetter() != null ? sub.getAwardedGradeLetter() : "N/A";
            gradeDist.put(grade, gradeDist.getOrDefault(grade, 0L) + 1);
        }

        BigDecimal avg = total.divide(BigDecimal.valueOf(completedSubs.size()), 2, RoundingMode.HALF_UP);
        BigDecimal passRate = BigDecimal.valueOf(passCount * 100.0 / completedSubs.size()).setScale(2, RoundingMode.HALF_UP);

        return ExamAnalyticsDTO.builder()
                .examId(examId)
                .title(exam.getTitle())
                .totalParticipants(completedSubs.size())
                .averageScore(avg)
                .highestScore(highest)
                .lowestScore(lowest)
                .passingRate(passRate)
                .gradeDistribution(gradeDist)
                .build();
    }
}
