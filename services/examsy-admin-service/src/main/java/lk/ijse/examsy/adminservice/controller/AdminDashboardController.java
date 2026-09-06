package lk.ijse.examsy.adminservice.controller;

import lk.ijse.examsy.adminservice.dto.AdminDashboardDTO;
import lk.ijse.examsy.adminservice.service.AdminDashboardService;
import lk.ijse.examsy.common.dto.APIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/dashboard/metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<AdminDashboardDTO>> getMetrics() {
        AdminDashboardDTO metrics = adminDashboardService.getDashboardMetrics();
        return ResponseEntity.ok(new APIResponse<>(200, "Metrics Fetched Successfully", metrics));
    }
}
