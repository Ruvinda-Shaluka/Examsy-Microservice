package lk.ijse.examsy.adminservice.service;

import lk.ijse.examsy.adminservice.dto.AdminDashboardDTO;

public interface AdminDashboardService {

    AdminDashboardDTO getDashboardMetrics();

    void recordUserRegistered(String role);

    void recordExamSubmitted();

    void recordGradeReleased();
}
