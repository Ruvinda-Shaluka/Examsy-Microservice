package lk.ijse.examsy.notificationservice.controller;

import jakarta.validation.Valid;
import lk.ijse.examsy.common.dto.APIResponse;
import lk.ijse.examsy.notificationservice.dto.DirectAlertRequestDTO;
import lk.ijse.examsy.notificationservice.dto.NotificationDTO;
import lk.ijse.examsy.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Validated
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<APIResponse<List<NotificationDTO>>> getNotifications(
            @AuthenticationPrincipal UserDetails user) {
        String username = user != null ? user.getUsername() : "anonymous";
        List<NotificationDTO> list = notificationService.getMyNotifications(username);
        return ResponseEntity.ok(new APIResponse<>(200, "Success", list));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<APIResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal UserDetails user) {
        String username = user != null ? user.getUsername() : "anonymous";
        long count = notificationService.getUnreadCount(username);
        return ResponseEntity.ok(new APIResponse<>(200, "Success", count));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<APIResponse<Void>> markAsRead(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails user) {
        String username = user != null ? user.getUsername() : "anonymous";
        notificationService.markAsRead(id, username);
        return ResponseEntity.ok(new APIResponse<>(200, "Marked as read", null));
    }

    @PutMapping("/read-all")
    public ResponseEntity<APIResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal UserDetails user) {
        String username = user != null ? user.getUsername() : "anonymous";
        notificationService.markAllAsRead(username);
        return ResponseEntity.ok(new APIResponse<>(200, "All marked as read", null));
    }

    @PostMapping("/direct")
    public ResponseEntity<APIResponse<Void>> sendDirectAlert(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody DirectAlertRequestDTO request) {
        String sender = user != null ? user.getUsername() : "system";
        notificationService.sendDirectAlert(sender, request);
        return ResponseEntity.ok(new APIResponse<>(200, "Direct alert dispatched successfully", null));
    }
}
