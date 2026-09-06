package lk.ijse.examsy.adminservice.service.impl;

import lk.ijse.examsy.adminservice.dto.AdminReportDTO;
import lk.ijse.examsy.adminservice.dto.ReportCreateDTO;
import lk.ijse.examsy.adminservice.entity.Report;
import lk.ijse.examsy.adminservice.event.AdminModerationAlertEvent;
import lk.ijse.examsy.adminservice.event.ClassReportedEvent;
import lk.ijse.examsy.adminservice.event.ClassTerminatedEvent;
import lk.ijse.examsy.adminservice.event.TeacherTerminatedEvent;
import lk.ijse.examsy.adminservice.kafka.AdminEventProducer;
import lk.ijse.examsy.adminservice.repository.ReportRepo;
import lk.ijse.examsy.adminservice.service.AdminReportService;
import lk.ijse.examsy.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminReportServiceImpl implements AdminReportService {

    private final ReportRepo reportRepository;
    private final AdminEventProducer adminEventProducer;

    @Override
    @Transactional(readOnly = true)
    public List<AdminReportDTO> getAllPendingReports() {
        return reportRepository.findByStatusOrderByReportedAtDesc("PENDING").stream()
                .map(this::mapToAdminReportDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void terminateClass(Integer reportId, String adminUsername) {
        Report report = findReportOrThrow(reportId);

        report.setStatus("RESOLVED");
        report.setAdminNotes("Class terminated by Admin.");
        report.setResolvedAt(LocalDateTime.now());
        report.setResolvedByAdminUsername(adminUsername);
        reportRepository.save(report);

        // 1. Emit ClassTerminatedEvent for class-service and dependent microservices
        adminEventProducer.sendClassTerminatedEvent(ClassTerminatedEvent.builder()
                .reportId(report.getId())
                .courseId(report.getTargetCourseId())
                .courseName(report.getTargetCourseName())
                .teacherUsername(report.getTargetTeacherUsername())
                .reporterUsername(report.getReporterStudentUsername())
                .reason("Class terminated due to policy violations.")
                .adminUsername(adminUsername)
                .terminatedAt(LocalDateTime.now())
                .build());

        // 2. Alert teacher
        if (report.getTargetTeacherUsername() != null) {
            adminEventProducer.sendAdminModerationAlert(AdminModerationAlertEvent.builder()
                    .recipientUsername(report.getTargetTeacherUsername())
                    .title("Notice: Class Terminated")
                    .message("Your class '" + report.getTargetCourseName() + "' was terminated due to policy violations.")
                    .courseId(null)
                    .actionType("WARNING")
                    .timestamp(LocalDateTime.now())
                    .build());
        }

        // 3. Acknowledge reporter student
        if (report.getReporterStudentUsername() != null) {
            adminEventProducer.sendAdminModerationAlert(AdminModerationAlertEvent.builder()
                    .recipientUsername(report.getReporterStudentUsername())
                    .title("Report Resolved")
                    .message("Action has been taken regarding your report on '" + report.getTargetCourseName() + "'.")
                    .courseId(null)
                    .actionType("NOTICE")
                    .timestamp(LocalDateTime.now())
                    .build());
        }

        log.info("Class {} terminated by admin {} via report {}", report.getTargetCourseId(), adminUsername, reportId);
    }

    @Override
    @Transactional
    public void terminateTeacher(Integer reportId, String adminUsername) {
        Report report = findReportOrThrow(reportId);

        report.setStatus("RESOLVED");
        report.setAdminNotes("Teacher account terminated by Admin.");
        report.setResolvedAt(LocalDateTime.now());
        report.setResolvedByAdminUsername(adminUsername);
        reportRepository.save(report);

        // 1. Emit TeacherTerminatedEvent for auth/profile services
        adminEventProducer.sendTeacherTerminatedEvent(TeacherTerminatedEvent.builder()
                .reportId(report.getId())
                .teacherId(report.getTargetTeacherId())
                .teacherUsername(report.getTargetTeacherUsername())
                .reason("Instructor account permanently terminated due to severe policy violations.")
                .adminUsername(adminUsername)
                .terminatedAt(LocalDateTime.now())
                .build());

        // 2. Alert teacher
        if (report.getTargetTeacherUsername() != null) {
            adminEventProducer.sendAdminModerationAlert(AdminModerationAlertEvent.builder()
                    .recipientUsername(report.getTargetTeacherUsername())
                    .title("Account Termination Notice")
                    .message("Your Examsy instructor account has been permanently terminated due to severe policy violations.")
                    .courseId(null)
                    .actionType("WARNING")
                    .timestamp(LocalDateTime.now())
                    .build());
        }

        log.info("Teacher {} terminated by admin {} via report {}", report.getTargetTeacherUsername(), adminUsername, reportId);
    }

    @Override
    @Transactional
    public void dismissReport(Integer reportId, String adminUsername) {
        Report report = findReportOrThrow(reportId);
        report.setStatus("DISMISSED");
        report.setResolvedAt(LocalDateTime.now());
        report.setResolvedByAdminUsername(adminUsername);
        reportRepository.save(report);
        log.info("Report {} dismissed by admin {}", reportId, adminUsername);
    }

    @Override
    @Transactional
    public void warnTeacher(Integer reportId, String adminUsername) {
        Report report = findReportOrThrow(reportId);

        report.setStatus("RESOLVED");
        report.setAdminNotes("Official warning sent to teacher. Student acknowledged.");
        report.setResolvedAt(LocalDateTime.now());
        report.setResolvedByAdminUsername(adminUsername);
        reportRepository.save(report);

        // 1. Send Warning to Teacher
        if (report.getTargetTeacherUsername() != null) {
            adminEventProducer.sendAdminModerationAlert(AdminModerationAlertEvent.builder()
                    .recipientUsername(report.getTargetTeacherUsername())
                    .title("Official Warning")
                    .message("Your class '" + report.getTargetCourseName() + "' has received complaints regarding policy violations. Please review your content immediately.")
                    .courseId(report.getTargetCourseId())
                    .actionType("WARNING")
                    .timestamp(LocalDateTime.now())
                    .build());
        }

        // 2. Acknowledge Student
        if (report.getReporterStudentUsername() != null) {
            adminEventProducer.sendAdminModerationAlert(AdminModerationAlertEvent.builder()
                    .recipientUsername(report.getReporterStudentUsername())
                    .title("Report Reviewed")
                    .message("We have reviewed your report on '" + report.getTargetCourseName() + "' and issued an official warning to the instructor.")
                    .courseId(report.getTargetCourseId())
                    .actionType("NOTICE")
                    .timestamp(LocalDateTime.now())
                    .build());
        }

        log.info("Official warning issued to teacher {} for report {}", report.getTargetTeacherUsername(), reportId);
    }

    @Override
    @Transactional
    public void replyToStudent(Integer reportId, String message, String adminUsername) {
        Report report = findReportOrThrow(reportId);

        String existingNotes = report.getAdminNotes() == null ? "" : report.getAdminNotes() + "\n";
        report.setAdminNotes(existingNotes + "Admin Replied to Student: " + message);
        reportRepository.save(report);

        // Send In-App / Email Alert to Student
        if (report.getReporterStudentUsername() != null) {
            adminEventProducer.sendAdminModerationAlert(AdminModerationAlertEvent.builder()
                    .recipientUsername(report.getReporterStudentUsername())
                    .title("Admin Update on Report #" + reportId)
                    .message(message)
                    .courseId(report.getTargetCourseId())
                    .actionType("REPLY")
                    .timestamp(LocalDateTime.now())
                    .build());
        }

        log.info("Admin reply sent to student {} for report {}", report.getReporterStudentUsername(), reportId);
    }

    @Override
    @Transactional
    public void fileReport(String reporterUsername, ReportCreateDTO dto) {
        String courseName = dto.getTargetCourseName() != null && !dto.getTargetCourseName().isBlank()
                ? dto.getTargetCourseName()
                : "Course #" + dto.getTargetCourseId();

        Report report = Report.builder()
                .reporterStudentUsername(reporterUsername)
                .reporterStudentName(reporterUsername)
                .targetCourseId(dto.getTargetCourseId())
                .targetCourseName(courseName)
                .targetTeacherId(dto.getTargetTeacherId())
                .targetTeacherUsername(dto.getTargetTeacherUsername())
                .targetTeacherName(dto.getTargetTeacherName())
                .category(dto.getCategory())
                .priorityLevel(dto.getPriorityLevel())
                .description(dto.getDescription())
                .status("PENDING")
                .build();

        reportRepository.save(report);
        log.info("Student '{}' filed report for course ID {}", reporterUsername, dto.getTargetCourseId());
    }

    @Override
    @Transactional
    public void processReportEvent(ClassReportedEvent event) {
        Report report = Report.builder()
                .reporterStudentId(event.getReporterStudentId())
                .reporterStudentUsername(event.getReporterStudentUsername())
                .reporterStudentName(event.getReporterStudentName())
                .targetCourseId(event.getTargetCourseId())
                .targetCourseName(event.getTargetCourseName())
                .targetTeacherId(event.getTargetTeacherId())
                .targetTeacherUsername(event.getTargetTeacherUsername())
                .targetTeacherName(event.getTargetTeacherName())
                .category(event.getCategory())
                .priorityLevel(event.getPriorityLevel())
                .description(event.getDescription())
                .status("PENDING")
                .build();

        reportRepository.save(report);
        log.info("Ingested ClassReportedEvent for course: {}", event.getTargetCourseName());
    }

    private Report findReportOrThrow(Integer reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + reportId));
    }

    private AdminReportDTO mapToAdminReportDTO(Report r) {
        String teacher = r.getTargetTeacherName() != null ? r.getTargetTeacherName() : r.getTargetTeacherUsername();
        String reporter = r.getReporterStudentName() != null ? r.getReporterStudentName() : r.getReporterStudentUsername();
        long complaintCount = reportRepository.countComplaintsByTeacher(r.getTargetTeacherUsername(), r.getTargetTeacherName());

        return AdminReportDTO.builder()
                .id(r.getId())
                .category(r.getCategory())
                .priorityLevel(r.getPriorityLevel())
                .description(r.getDescription())
                .status(r.getStatus())
                .reportedAt(r.getReportedAt())
                .classId(r.getTargetCourseId())
                .className(r.getTargetCourseName())
                .teacherName(teacher != null ? teacher : "Instructor")
                .reporterName(reporter != null ? reporter : "Anonymous Student")
                .teacherComplaintCount(Math.max(1L, complaintCount))
                .build();
    }
}
