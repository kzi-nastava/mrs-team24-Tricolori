package com.tricolori.backend.controller;

import com.tricolori.backend.dto.notifications.NotificationDto;
import com.tricolori.backend.enums.NotificationType;
import com.tricolori.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
public class NotificationController {

    private final NotificationService notificationService;

    // Get all notifications for the authenticated user
    @GetMapping
    public ResponseEntity<List<NotificationDto>> getAllNotifications(Authentication authentication) {
        String email = authentication.getName();
        log.info("GET /api/notifications - user: {}", email);

        List<NotificationDto> notifications = notificationService.getAllNotifications(email);
        return ResponseEntity.ok(notifications);
    }

    // Get unread notifications for the authenticated user
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDto>> getUnreadNotifications(Authentication authentication) {
        String email = authentication.getName();
        log.info("GET /api/notifications/unread - user: {}", email);

        List<NotificationDto> notifications = notificationService.getUnreadNotifications(email);
        return ResponseEntity.ok(notifications);
    }

    // Get count of unread notifications
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        String email = authentication.getName();
        log.info("GET /api/notifications/unread/count - user: {}", email);

        long count = notificationService.getUnreadCount(email);
        return ResponseEntity.ok(Map.of("count", count));
    }

    // Mark a specific notification as read
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationDto> markAsRead(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        log.info("PATCH /api/notifications/{}/read - user: {}", id, email);

        try {
            NotificationDto notification = notificationService.markAsRead(id);

            // Verify that the notification belongs to the authenticated user
            if (!notification.getEmail().equals(email)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            return ResponseEntity.ok(notification);
        } catch (RuntimeException e) {
            log.error("Error marking notification as read: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // Mark all notifications as read for the authenticated user
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        String email = authentication.getName();
        log.info("PATCH /api/notifications/read-all - user: {}", email);

        notificationService.markAllAsRead(email);
        return ResponseEntity.noContent().build();
    }

    // Delete a specific notification
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        log.info("DELETE /api/notifications/{} - user: {}", id, email);

        // Note: You might want to add a check to ensure the notification belongs to the user
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }

    // Delete all notifications for the authenticated user
    @DeleteMapping
    public ResponseEntity<Void> deleteAllNotifications(Authentication authentication) {
        String email = authentication.getName();
        log.info("DELETE /api/notifications - user: {}", email);

        notificationService.deleteAllNotifications(email);
        return ResponseEntity.noContent().build();
    }

    // Test endpoint to send a notification (for development/testing)
    // TODO: Remove after testing
    @PostMapping("/test")
    public ResponseEntity<NotificationDto> sendTestNotification(
            Authentication authentication,
            @RequestParam(required = false) NotificationType type,
            @RequestParam(required = false) Long rideId,
            @RequestParam(required = false) String content) {

        String email = authentication.getName();
        log.info("POST /api/notifications/test - user: {}", email);

        NotificationType notifType = type != null ? type : NotificationType.GENERAL;
        String notifContent = content != null ? content : "This is a test notification";

        NotificationDto notification = notificationService.sendNotification(email, notifContent, notifType, rideId);
        return ResponseEntity.status(HttpStatus.CREATED).body(notification);
    }
}