package com.testing.traningproject.model.dto.response;

import com.testing.traningproject.model.enums.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Notification Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Long id;
    private NotificationType notificationType;
    private String title;
    private String message;
    private Boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}

