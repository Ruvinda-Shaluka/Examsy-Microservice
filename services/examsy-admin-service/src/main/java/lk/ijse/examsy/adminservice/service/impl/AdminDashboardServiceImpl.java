package lk.ijse.examsy.adminservice.service.impl;

import lk.ijse.examsy.adminservice.dto.AdminDashboardDTO;
import lk.ijse.examsy.adminservice.dto.ReportDistributionDTO;
import lk.ijse.examsy.adminservice.entity.PlatformMetric;
import lk.ijse.examsy.adminservice.repository.PlatformMetricRepo;
import lk.ijse.examsy.adminservice.repository.ReportRepo;
import lk.ijse.examsy.adminservice.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final ReportRepo reportRepository;
    private final PlatformMetricRepo metricRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardDTO getDashboardMetrics() {
        long totalStudents = getMetricValue("TOTAL_STUDENTS");
        long activeTeachers = getMetricValue("ACTIVE_TEACHERS");
        long totalUsers = getMetricValue("TOTAL_USERS");
        long pendingReports = reportRepository.countByStatus("PENDING");

        List<ReportDistributionDTO> distribution = reportRepository.countReportsByCategory();

        return AdminDashboardDTO.builder()
                .totalStudents(totalStudents)
                .activeTeachers(activeTeachers)
                .pendingReports(pendingReports)
                .totalUsers(totalUsers)
                .reportDistribution(distribution)
                .build();
    }

    @Override
    @Transactional
    public void recordUserRegistered(String role) {
        incrementMetricSafely("TOTAL_USERS", 1);
        if ("STUDENT".equalsIgnoreCase(role)) {
            incrementMetricSafely("TOTAL_STUDENTS", 1);
        } else if ("TEACHER".equalsIgnoreCase(role)) {
            incrementMetricSafely("ACTIVE_TEACHERS", 1);
        }
        log.info("Recorded new user registration for role: {}", role);
    }

    @Override
    @Transactional
    public void recordExamSubmitted() {
        incrementMetricSafely("TOTAL_EXAMS_SUBMITTED", 1);
        log.debug("Recorded exam submission in platform metrics");
    }

    @Override
    @Transactional
    public void recordGradeReleased() {
        incrementMetricSafely("TOTAL_GRADES_FINALIZED", 1);
        log.debug("Recorded grade release in platform metrics");
    }

    private long getMetricValue(String key) {
        return metricRepository.findByMetricKey(key)
                .map(PlatformMetric::getMetricValue)
                .orElse(0L);
    }

    private void incrementMetricSafely(String key, long delta) {
        int updated = metricRepository.incrementMetric(key, delta);
        if (updated == 0) {
            metricRepository.save(PlatformMetric.builder()
                    .metricKey(key)
                    .metricValue(delta)
                    .description("Auto-initialized metric: " + key)
                    .build());
        }
    }
}
