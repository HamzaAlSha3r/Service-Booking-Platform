package com.testing.traningproject.service;

import com.testing.traningproject.mapper.TransactionMapper;
import com.testing.traningproject.model.dto.response.TransactionResponse;
import com.testing.traningproject.model.entity.Transaction;
import com.testing.traningproject.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for Transaction operations
 * Handles transaction retrieval and history
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper; // ✅ MapStruct mapper

    /**
     * Get all transactions for a user
     */
    @Transactional(readOnly = true)
    public List<TransactionResponse> getMyTransactions(Long userId) {
        log.info("Fetching all transactions for user ID: {}", userId);

        List<Transaction> transactions = transactionRepository.findByUserId(userId);
        return transactionMapper.toResponseList(transactions);
    }

    /**
     * Get all transactions (Admin only)
     */
    @Transactional(readOnly = true)
    public List<TransactionResponse> getAllTransactions() {
        log.info("Fetching all transactions (Admin)");

        List<Transaction> transactions = transactionRepository.findAll();
        return transactionMapper.toResponseList(transactions);
    }
}

