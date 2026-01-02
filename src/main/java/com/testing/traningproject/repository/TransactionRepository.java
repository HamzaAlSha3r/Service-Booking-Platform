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

    // Count transactions by status
    long countByStatus(TransactionStatus status);

    // Count transactions by type and status
    long countByTransactionTypeAndStatus(TransactionType transactionType, TransactionStatus status);
}



