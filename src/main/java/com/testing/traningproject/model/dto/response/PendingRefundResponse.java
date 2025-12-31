package com.testing.traningproject.model.dto.response;

import com.testing.traningproject.model.enums.RefundStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for pending refund request
 * Returns refund details for admin decision
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingRefundResponse {

    private Long id;
    private Long bookingId;
    private String customerName;
    private String customerEmail;
    private String serviceName;
    private String providerName;
    private BigDecimal refundAmount;
    private String refundReason;
    private RefundStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime bookingDate;
}

