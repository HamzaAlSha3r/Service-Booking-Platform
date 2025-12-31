package com.testing.traningproject.repository;

import com.testing.traningproject.model.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Transaction entity
 * Provides database access methods for Transaction table
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}

