package lk.ijse.examsy.adminservice.service;

import lk.ijse.examsy.adminservice.dto.AdminReportDTO;
import lk.ijse.examsy.adminservice.dto.ReportCreateDTO;
import lk.ijse.examsy.adminservice.event.ClassReportedEvent;

import java.util.List;

public interface AdminReportService {

    List<AdminReportDTO> getAllPendingReports();

    void terminateClass(Integer reportId, String adminUsername);

    void terminateTeacher(Integer reportId, String adminUsername);

    void dismissReport(Integer reportId, String adminUsername);

    void warnTeacher(Integer reportId, String adminUsername);

    void replyToStudent(Integer reportId, String message, String adminUsername);

    void fileReport(String reporterUsername, ReportCreateDTO dto);

    void processReportEvent(ClassReportedEvent event);
}
