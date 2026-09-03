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
        return Collections.emptyList(); // Will be committed next
    }

    @Transactional
    @Override
    public void deleteExam(String teacherUsername, Integer examId) {
        // Will be committed next
    }

    @Transactional
    @Override
    public void updateExamTiming(String teacherUsername, Integer examId, UpdateExamDeadlineDTO dto) {
        // Will be committed next
    }

    @Transactional(readOnly = true)
    @Override
    public OngoingExamGroupDTO getOngoingExams(String teacherUsername) {
        return null; // Will be committed next
    }

    @Transactional(readOnly = true)
    @Override
    public List<LiveStudentMonitorDTO> getLiveMonitorData(Integer examId, String teacherUsername) {
        return Collections.emptyList(); // Will be committed next
    }

    @Transactional
    @Override
    public void broadcastToExam(Integer examId, String teacherUsername, String message) {
        // Will be committed next
    }

    @Transactional
    @Override
    public void warnStudent(Integer examId, Integer studentId, String teacherUsername, String message) {
        // Will be committed next
    }

    @Transactional(readOnly = true)
    @Override
    public ExamAnalyticsDTO getExamAnalytics(Integer examId, String teacherUsername) {
        return null; // Will be committed next
    }
}
