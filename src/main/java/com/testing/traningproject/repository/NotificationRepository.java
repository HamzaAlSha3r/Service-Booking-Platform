package com.testing.traningproject.repository;

import com.testing.traningproject.model.entity.Notification;
import com.testing.traningproject.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Notification Repository
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Find all notifications for a user ordered by creation date (newest first)
     */
    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Find unread notifications for a user
     */
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);

    /**
     * Count unread notifications for a user
     */
    Long countByUserAndIsReadFalse(User user);
}

