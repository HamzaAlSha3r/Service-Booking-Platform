package com.testing.traningproject.controller;

import com.testing.traningproject.model.dto.response.NotificationResponse;
import com.testing.traningproject.security.CustomUserDetails;
import com.testing.traningproject.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Notification Controller - Manage user notifications
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Get all notifications for current user
     */
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<NotificationResponse> notifications = notificationService.getUserNotifications(userDetails.getId());
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get unread notifications only
     */
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<NotificationResponse> notifications = notificationService.getUnreadNotifications(userDetails.getId());
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get unread notifications count
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long count = notificationService.getUnreadCount(userDetails.getId());

        Map<String, Long> response = new HashMap<>();
        response.put("unreadCount", count);

        return ResponseEntity.ok(response);
    }

    /**
     * Mark notification as read
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        NotificationResponse notification = notificationService.markAsRead(id, userDetails.getId());
        return ResponseEntity.ok(notification);
    }

    /**
     * Mark all notifications as read
     */
    @PutMapping("/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        notificationService.markAllAsRead(userDetails.getId());

        Map<String, String> response = new HashMap<>();
        response.put("message", "All notifications marked as read");

        return ResponseEntity.ok(response);
    }

    /**
     * Delete notification
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteNotification(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        notificationService.deleteNotification(id, userDetails.getId());

        Map<String, String> response = new HashMap<>();
        response.put("message", "Notification deleted successfully");

        return ResponseEntity.ok(response);
    }
}

