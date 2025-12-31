package com.testing.traningproject.repository;

import com.testing.traningproject.model.entity.Refund;
import com.testing.traningproject.model.enums.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Refund entity
 * Provides database access methods for Refund table
 */
@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

    /**
     * Find all refunds by status
     * Used by admin to get pending refund requests
     */
    List<Refund> findByStatus(RefundStatus status);

    /**
     * Find refund by booking ID
     * Used to check if refund already exists for a booking
     */
    boolean existsByBookingId(Long bookingId);
}

