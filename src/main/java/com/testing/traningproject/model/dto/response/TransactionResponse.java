package com.testing.traningproject.model.dto.response;

import com.testing.traningproject.model.enums.TransactionStatus;
import com.testing.traningproject.model.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for Transaction details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private Long id;
    private Long userId;
    private String userName;
    private Long bookingId;
    private Long subscriptionId;
    private TransactionType transactionType;
    private BigDecimal amount;
    private String paymentMethod;
    private String paymentGatewayTransactionId;
    private TransactionStatus status;
    private String failureReason;
    private LocalDateTime createdAt;
}

