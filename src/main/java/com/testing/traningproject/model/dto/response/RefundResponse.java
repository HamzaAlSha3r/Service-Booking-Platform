package com.testing.traningproject.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for Refund
 * Shows refund request status and details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundResponse {
    private Long id;
    private Long bookingId;
    private String serviceTitle;
    private BigDecimal refundAmount;
    private String refundReason;
    private String status; // PENDING, APPROVED, REJECTED, COMPLETED
    private String adminNotes;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
}

