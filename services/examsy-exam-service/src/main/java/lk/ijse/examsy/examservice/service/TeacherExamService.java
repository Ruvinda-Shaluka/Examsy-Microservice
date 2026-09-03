package lk.ijse.examsy.examservice.service;

import lk.ijse.examsy.examservice.dto.*;

import java.util.List;

public interface TeacherExamService {
    void publishExam(String teacherUsername, ExamPublishDTO dto);
    List<ExamSummaryDTO> getClassExams(String teacherUsername, Integer classId);
    void deleteExam(String teacherUsername, Integer examId);
    void updateExamTiming(String teacherUsername, Integer examId, UpdateExamDeadlineDTO dto);
    OngoingExamGroupDTO getOngoingExams(String teacherUsername);
    List<LiveStudentMonitorDTO> getLiveMonitorData(Integer examId, String teacherUsername);
    void broadcastToExam(Integer examId, String teacherUsername, String message);
    void warnStudent(Integer examId, Integer studentId, String teacherUsername, String message);
    ExamAnalyticsDTO getExamAnalytics(Integer examId, String teacherUsername);
}
