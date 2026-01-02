package com.testing.traningproject.service;

import com.testing.traningproject.model.dto.response.TransactionResponse;
import com.testing.traningproject.model.entity.Transaction;
import com.testing.traningproject.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for Transaction operations
 * Handles transaction retrieval and history
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;

    /**
     * Get all transactions for a user
     */
    @Transactional(readOnly = true)
    public List<TransactionResponse> getMyTransactions(Long userId) {
        log.info("Fetching all transactions for user ID: {}", userId);

        List<Transaction> transactions = transactionRepository.findByUserId(userId);
        return transactions.stream()
                .map(this::convertToTransactionResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all transactions (Admin only)
     */
    @Transactional(readOnly = true)
    public List<TransactionResponse> getAllTransactions() {
        log.info("Fetching all transactions (Admin)");

        List<Transaction> transactions = transactionRepository.findAll();
        return transactions.stream()
                .map(this::convertToTransactionResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert Transaction entity to TransactionResponse DTO
     */
    private TransactionResponse convertToTransactionResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .userId(transaction.getUser().getId())
                .userName(transaction.getUser().getFirstName() + " " + transaction.getUser().getLastName())
                .bookingId(transaction.getBooking() != null ? transaction.getBooking().getId() : null)
                .subscriptionId(transaction.getSubscription() != null ? transaction.getSubscription().getId() : null)
                .transactionType(transaction.getTransactionType())
                .amount(transaction.getAmount())
                .paymentMethod(transaction.getPaymentMethod())
                .paymentGatewayTransactionId(transaction.getPaymentGatewayTransactionId())
                .status(transaction.getStatus())
                .failureReason(transaction.getFailureReason())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}

