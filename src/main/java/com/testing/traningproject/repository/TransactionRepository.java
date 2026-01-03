package com.testing.traningproject.repository;

import com.testing.traningproject.model.entity.Transaction;
import com.testing.traningproject.model.enums.TransactionStatus;
import com.testing.traningproject.model.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository for Transaction entity
 * Provides database access methods for Transaction table
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Find all transactions for a user
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId ORDER BY t.createdAt DESC")
    List<Transaction> findByUserId(@Param("userId") Long userId);

    // Find transaction by booking ID and type
    @Query("SELECT t FROM Transaction t WHERE t.booking.id = :bookingId AND t.transactionType = :type")
    Transaction findByBookingIdAndTransactionType(@Param("bookingId") Long bookingId, @Param("type") TransactionType type);

    // Find transactions by type
    List<Transaction> findByTransactionType(TransactionType transactionType);

    // Find transactions by type and status
    List<Transaction> findByTransactionTypeAndStatus(TransactionType transactionType, TransactionStatus status);

    // Calculate total revenue (all successful booking payments)
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.transactionType = 'BOOKING_PAYMENT' AND t.status = 'SUCCESS'")
    BigDecimal calculateTotalRevenue();

    // Calculate total refund amount
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.transactionType = 'REFUND' AND t.status = 'SUCCESS'")
    BigDecimal calculateTotalRefundAmount();

    // Sum successful transactions by type
    @Query("SELECT SUM(t.amount) FROM Transaction t " +
           "WHERE t.transactionType = :type AND t.status = 'SUCCESS'")
    BigDecimal sumSuccessfulTransactionsByType(@Param("type") TransactionType type);

    // Sum pending transactions by type
    @Query("SELECT SUM(t.amount) FROM Transaction t " +
           "WHERE t.transactionType = :type AND t.status = 'PENDING'")
    BigDecimal sumPendingTransactionsByType(@Param("type") TransactionType type);

    // Count transactions by status
    long countByStatus(TransactionStatus status);

    // Count transactions by type and status
    long countByTransactionTypeAndStatus(TransactionType transactionType, TransactionStatus status);
}



