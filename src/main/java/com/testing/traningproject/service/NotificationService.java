package com.testing.traningproject.service;

import com.testing.traningproject.exception.ResourceNotFoundException;
import com.testing.traningproject.mapper.NotificationMapper;
import com.testing.traningproject.model.dto.response.NotificationResponse;
import com.testing.traningproject.model.entity.Notification;
import com.testing.traningproject.model.entity.User;
import com.testing.traningproject.model.enums.NotificationType;
import com.testing.traningproject.repository.NotificationRepository;
import com.testing.traningproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Notification Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper; // ✅ MapStruct mapper

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
     * Get all notifications for user
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        return notificationMapper.toResponseList(notifications);
    }

    /**
     * Get unread notifications only
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Notification> notifications = notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
        return notificationMapper.toResponseList(notifications);
    }

    /**
     * Get unread notifications count
     */
    @Transactional(readOnly = true)
    public Long getUnreadCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    /**
     * Mark notification as read
     */
    @Transactional
    public NotificationResponse markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        // Verify ownership
        if (!notification.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Notification not found");
        }

        if (!notification.getIsRead()) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
            log.info("Notification {} marked as read by user ID: {}", notificationId, userId);
        }

        return notificationMapper.toResponse(notification);
    }

    /**
     * Mark all notifications as read
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Notification> unreadNotifications = notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
        LocalDateTime now = LocalDateTime.now();

        unreadNotifications.forEach(notification -> {
            notification.setIsRead(true);
            notification.setReadAt(now);
        });

        notificationRepository.saveAll(unreadNotifications);
        log.info("All notifications marked as read for user ID: {}", userId);
    }

    /**
     * Delete notification
     */
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        // Verify ownership
        if (!notification.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Notification not found");
        }

        notificationRepository.delete(notification);
        log.info("Notification {} deleted by user ID: {}", notificationId, userId);
    }
}

