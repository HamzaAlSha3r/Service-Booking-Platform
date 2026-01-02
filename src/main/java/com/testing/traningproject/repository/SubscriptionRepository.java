package com.testing.traningproject.repository;

import com.testing.traningproject.model.entity.Subscription;
import com.testing.traningproject.model.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Subscription entity
 * Provides database access methods for Subscription table
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    /**
     * Find active subscription for a specific provider
     * Used to check if provider has valid subscription before creating services
     */
    @Query("SELECT s FROM Subscription s WHERE s.provider.id = :providerId AND s.status = 'ACTIVE' AND s.endDate >= CURRENT_DATE ORDER BY s.endDate DESC")
    Optional<Subscription> findActiveSubscriptionByProviderId(@Param("providerId") Long providerId);

    /**
     * Count subscriptions by plan ID and status
     * Used to check if plan has active subscriptions before deletion
     */
    long countByPlanIdAndStatus(Integer planId, SubscriptionStatus status);

    /**
     * Find all subscriptions by provider ID
     * Used to view provider's subscription history
     */
    List<Subscription> findByProviderId(Long providerId);

    /**
     * Find all subscriptions by status
     * Used by admin to get statistics
     */
    Long countByStatus(SubscriptionStatus status);

    /**
     * Find subscriptions expiring soon (for auto-renewal)
     * Used by scheduled job to send expiration warnings
     */
    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' AND s.endDate BETWEEN :startDate AND :endDate AND s.autoRenew = true")
    List<Subscription> findExpiringSoon(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Find expired subscriptions that need status update
     * Used by scheduled job to mark subscriptions as EXPIRED
     */
    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' AND s.endDate < CURRENT_DATE")
    List<Subscription> findExpiredSubscriptions();
}

