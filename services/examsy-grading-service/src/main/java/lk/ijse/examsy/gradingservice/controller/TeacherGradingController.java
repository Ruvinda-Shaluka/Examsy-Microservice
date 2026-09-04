package lk.ijse.examsy.gradingservice.controller;

import lk.ijse.examsy.common.dto.APIResponse;
import lk.ijse.examsy.gradingservice.dto.PendingGradingDTO;
import lk.ijse.examsy.gradingservice.service.SmartGradingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/teacher/exams")
@RequiredArgsConstructor
@Slf4j
public class TeacherGradingController {

    private final SmartGradingService smartGradingService;

    @GetMapping("/pending-gradings")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<APIResponse<List<PendingGradingDTO>>> getPendingGradings(
            @AuthenticationPrincipal UserDetails user) {
        String username = user != null ? user.getUsername() : null;
        List<PendingGradingDTO> data = smartGradingService.getPendingPdfGradings(username);
        return ResponseEntity.ok(new APIResponse<>(200, "Pending gradings fetched", data));
    }

    @PostMapping("/{examId}/grade/{submissionId}/auto")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<APIResponse<Map<String, Object>>> autoGradePdfSubmission(
            @PathVariable Integer examId,
            @PathVariable Integer submissionId) {
        log.info("Auto-grading requested for examId: {}, submissionId: {}", examId, submissionId);
        Map<String, Object> gradingResult = smartGradingService.autoGradeSubmission(examId, submissionId);
        return ResponseEntity.ok(new APIResponse<>(200, "Auto-grading completed successfully", gradingResult));
    }

    @PostMapping("/{examId}/grade/{submissionId}/approve")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<APIResponse<Void>> approveGrade(
            @PathVariable Integer examId,
            @PathVariable Integer submissionId,
            @RequestParam(name = "score", required = false) BigDecimal scoreParam,
            @RequestBody(required = false) Map<String, Object> payload,
            @AuthenticationPrincipal UserDetails user) {

        BigDecimal score = scoreParam;
        BigDecimal aiScore = null;
        String comments = "Manually Graded";

        if (payload != null) {
            if (score == null && payload.get("score") != null) {
                score = new BigDecimal(payload.get("score").toString());
            }
            if (payload.get("aiScore") != null) {
                aiScore = new BigDecimal(payload.get("aiScore").toString());
            }
            if (payload.get("comments") != null) {
                comments = payload.get("comments").toString();
            }
        }
        if (score == null) {
            score = BigDecimal.ZERO;
        }

        String username = user != null ? user.getUsername() : "teacher";
        smartGradingService.approveAndReleaseGrade(username, examId, submissionId, score, aiScore, comments);
        return ResponseEntity.ok(new APIResponse<>(200, "Grade released successfully", null));
    }
}
