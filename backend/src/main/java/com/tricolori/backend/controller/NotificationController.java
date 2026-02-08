package com.tricolori.backend.controller;

import com.tricolori.backend.dto.notifications.NotificationDto;
import com.tricolori.backend.entity.Person;
import com.tricolori.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // Get all notifications for the authenticated user
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationDto>> getAllNotifications(
            @AuthenticationPrincipal Person person
    ) {
        List<NotificationDto> notifications = notificationService.getAllNotifications(person.getEmail());
        return ResponseEntity.ok(notifications);
    }

    // Get only unread notifications
    @GetMapping("/unread")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationDto>> getUnreadNotifications(
            @AuthenticationPrincipal Person person
    ) {
        List<NotificationDto> notifications = notificationService.getUnreadNotifications(person.getEmail());
        return ResponseEntity.ok(notifications);
    }

    // Get count of unread notifications (for badge display)
    @GetMapping("/unread/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> getUnreadCount(
            @AuthenticationPrincipal Person person
    ) {
        long count = notificationService.getUnreadCount(person.getEmail());
        return ResponseEntity.ok(count);
    }

    // Mark a single notification as read
    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificationDto> markAsRead(
            @PathVariable Long id
    ) {
        NotificationDto notification = notificationService.markAsRead(id);
        return ResponseEntity.ok(notification);
    }

    // Mark all notifications as read for the user
    @PutMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal Person person
    ) {
        notificationService.markAllAsRead(person.getEmail());
        return ResponseEntity.ok().build();
    }

    // Delete a single notification
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long id
    ) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok().build();
    }

    // Delete all notifications for the user
    @DeleteMapping("/all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteAllNotifications(
            @AuthenticationPrincipal Person person
    ) {
        notificationService.deleteAllNotifications(person.getEmail());
        return ResponseEntity.ok().build();
    }
}