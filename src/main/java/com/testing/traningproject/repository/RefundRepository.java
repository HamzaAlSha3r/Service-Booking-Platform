package com.testing.traningproject.repository;

import com.testing.traningproject.model.entity.Refund;
import com.testing.traningproject.model.enums.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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

    /**
     * Find all refunds by customer ID
     * Used to show customer their refund requests
     */
    List<Refund> findByBookingCustomerId(Long customerId);

    /**
     * Count refunds by status
     */
    long countByStatus(RefundStatus status);

    /**
     * Sum refund amounts by status
     */
    @Query("SELECT SUM(r.refundAmount) FROM Refund r WHERE r.status = :status")
    BigDecimal sumRefundAmountByStatus(@Param("status") RefundStatus status);
}

