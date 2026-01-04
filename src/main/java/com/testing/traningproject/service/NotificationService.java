package com.testing.traningproject.service;

import com.testing.traningproject.exception.ResourceNotFoundException;
import com.testing.traningproject.model.dto.response.NotificationResponse;
import com.testing.traningproject.model.entity.Notification;
import com.testing.traningproject.model.entity.User;
import com.testing.traningproject.model.enums.NotificationType;
import com.testing.traningproject.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Notification Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void createNotification(User user, NotificationType type, String title, String message) {
        Notification notification = Notification.builder()
                .user(user)
                .notificationType(type)
                .title(title)
                .message(message)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
        log.info("Notification created for user: {} - Type: {}", user.getEmail(), type);
    }

    /**
     * جلب جميع الإشعارات للمستخدم
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(User user) {
        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        return notifications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * جلب الإشعارات غير المقروءة فقط
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(User user) {
        List<Notification> notifications = notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
        return notifications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * عدد الإشعارات غير المقروءة
     */
    @Transactional(readOnly = true)
    public Long getUnreadCount(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    /**
     * تعليم إشعار كـ مقروء
     */
    @Transactional
    public NotificationResponse markAsRead(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        // التحقق من ملكية الإشعار
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Notification not found");
        }

        if (!notification.getIsRead()) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
            log.info("Notification {} marked as read by user {}", notificationId, user.getEmail());
        }

        return convertToResponse(notification);
    }

    /**
     * تعليم جميع الإشعارات كـ مقروءة
     */
    @Transactional
    public void markAllAsRead(User user) {
        List<Notification> unreadNotifications = notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
        LocalDateTime now = LocalDateTime.now();

        unreadNotifications.forEach(notification -> {
            notification.setIsRead(true);
            notification.setReadAt(now);
        });

        notificationRepository.saveAll(unreadNotifications);
        log.info("All notifications marked as read for user {}", user.getEmail());
    }

    /**
     * حذف إشعار
     */
    @Transactional
    public void deleteNotification(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        // التحقق من ملكية الإشعار
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Notification not found");
        }

        notificationRepository.delete(notification);
        log.info("Notification {} deleted by user {}", notificationId, user.getEmail());
    }

    /**
     * تحويل Notification إلى NotificationResponse
     */
    private NotificationResponse convertToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .notificationType(notification.getNotificationType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}

