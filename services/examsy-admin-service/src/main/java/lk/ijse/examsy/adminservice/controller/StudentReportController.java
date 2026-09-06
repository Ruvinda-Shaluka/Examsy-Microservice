package lk.ijse.examsy.adminservice.controller;

import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/student/dashboard/classes")
@RequiredArgsConstructor
@Validated
public class StudentReportController {

    private final AdminReportService adminReportService;

    @PostMapping("/report")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<APIResponse<Void>> reportClass(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ReportCreateDTO dto) {
        adminReportService.fileReport(userDetails.getUsername(), dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new APIResponse<>(201, "Report submitted successfully", null));
    }
}
