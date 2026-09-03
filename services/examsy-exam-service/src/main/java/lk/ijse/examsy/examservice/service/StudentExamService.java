package lk.ijse.examsy.examservice.service;

import lk.ijse.examsy.examservice.dto.*;

public interface StudentExamService {
    StudentExamViewDTO getExamForStudent(String studentUsername, Integer examId);
    ExamResultDTO submitExam(String studentUsername, Integer examId, ExamSubmitDTO dto);
    VaultExamsResponseDTO getVaultExams(String studentUsername, Integer classId);
    ProctoringStatsDTO logProctoringEvent(Integer examId, String studentUsername, ProctoringLogDTO logDTO);
    void logSecurityViolation(String studentUsername, ProctoringDTO dto);
    StudentAnalyticsDTO getStudentAnalytics(String studentUsername);
}
