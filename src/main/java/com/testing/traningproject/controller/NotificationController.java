package com.testing.traningproject.controller;

import com.testing.traningproject.model.dto.response.NotificationResponse;
import com.testing.traningproject.model.entity.User;
import com.testing.traningproject.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Notification Controller - إدارة الإشعارات
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * جلب جميع الإشعارات للمستخدم الحالي
     */
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = (User) userDetails;
        List<NotificationResponse> notifications = notificationService.getUserNotifications(user);
        return ResponseEntity.ok(notifications);
    }

    /**
     * جلب الإشعارات غير المقروءة فقط
     */
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = (User) userDetails;
        List<NotificationResponse> notifications = notificationService.getUnreadNotifications(user);
        return ResponseEntity.ok(notifications);
    }

    /**
     * عدد الإشعارات غير المقروءة
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = (User) userDetails;
        Long count = notificationService.getUnreadCount(user);

        Map<String, Long> response = new HashMap<>();
        response.put("unreadCount", count);

        return ResponseEntity.ok(response);
    }

    /**
     * تعليم إشعار كـ مقروء
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = (User) userDetails;
        NotificationResponse notification = notificationService.markAsRead(id, user);
        return ResponseEntity.ok(notification);
    }

    /**
     * تعليم جميع الإشعارات كـ مقروءة
     */
    @PutMapping("/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = (User) userDetails;
        notificationService.markAllAsRead(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "All notifications marked as read");

        return ResponseEntity.ok(response);
    }

    /**
     * حذف إشعار
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteNotification(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = (User) userDetails;
        notificationService.deleteNotification(id, user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Notification deleted successfully");

        return ResponseEntity.ok(response);
    }
}

