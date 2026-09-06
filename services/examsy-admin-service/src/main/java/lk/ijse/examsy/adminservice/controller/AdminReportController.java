package lk.ijse.examsy.adminservice.controller;

import lk.ijse.examsy.adminservice.dto.AdminReportDTO;
import lk.ijse.examsy.adminservice.dto.ReportCreateDTO;
import lk.ijse.examsy.adminservice.service.AdminReportService;
import lk.ijse.examsy.common.dto.APIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/reports")
@RequiredArgsConstructor
@Validated
public class AdminReportController {

    private final AdminReportService adminReportService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<List<AdminReportDTO>>> getReports() {
        return ResponseEntity.ok(new APIResponse<>(200, "Success", adminReportService.getAllPendingReports()));
    }

    @DeleteMapping("/{reportId}/terminate-class")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<Void>> terminateClass(
            @PathVariable Integer reportId,
            @AuthenticationPrincipal UserDetails adminUser) {
        adminReportService.terminateClass(reportId, adminUser.getUsername());
        return ResponseEntity.ok(new APIResponse<>(200, "Class terminated", null));
    }

    @DeleteMapping("/{reportId}/terminate-teacher")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<Void>> terminateTeacher(
            @PathVariable Integer reportId,
            @AuthenticationPrincipal UserDetails adminUser) {
        adminReportService.terminateTeacher(reportId, adminUser.getUsername());
        return ResponseEntity.ok(new APIResponse<>(200, "Teacher terminated", null));
    }

    @PutMapping("/{reportId}/dismiss")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<Void>> dismissReport(
            @PathVariable Integer reportId,
            @AuthenticationPrincipal UserDetails adminUser) {
        adminReportService.dismissReport(reportId, adminUser.getUsername());
        return ResponseEntity.ok(new APIResponse<>(200, "Report dismissed", null));
    }

    @PostMapping("/{reportId}/warn-teacher")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<Void>> warnTeacher(
            @PathVariable Integer reportId,
            @AuthenticationPrincipal UserDetails adminUser) {
        adminReportService.warnTeacher(reportId, adminUser.getUsername());
        return ResponseEntity.ok(new APIResponse<>(200, "Warning sent to teacher.", null));
    }

    @PostMapping("/{reportId}/reply-student")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<Void>> replyToStudent(
            @PathVariable Integer reportId,
            @RequestParam String message,
            @AuthenticationPrincipal UserDetails adminUser) {
        adminReportService.replyToStudent(reportId, message, adminUser.getUsername());
        return ResponseEntity.ok(new APIResponse<>(200, "Reply sent to student.", null));
    }

    @PostMapping("/file")
    public ResponseEntity<APIResponse<Void>> fileReport(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody ReportCreateDTO dto) {
        adminReportService.fileReport(user.getUsername(), dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new APIResponse<>(201, "Report submitted successfully", null));
    }
}
