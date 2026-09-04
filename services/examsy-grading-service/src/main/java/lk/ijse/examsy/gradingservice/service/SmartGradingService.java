package lk.ijse.examsy.gradingservice.service;

import lk.ijse.examsy.gradingservice.dto.PendingGradingDTO;
import lk.ijse.examsy.gradingservice.event.ExamSubmittedEvent;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface SmartGradingService {

    Map<String, Object> autoGradeSubmission(Integer examId, Integer submissionId);

    List<PendingGradingDTO> getPendingPdfGradings(String teacherUsername);

    void approveAndReleaseGrade(String teacherUsername, Integer examId, Integer submissionId, BigDecimal score, BigDecimal aiScore, String comments);

    void processKafkaExamSubmission(ExamSubmittedEvent event);
}
