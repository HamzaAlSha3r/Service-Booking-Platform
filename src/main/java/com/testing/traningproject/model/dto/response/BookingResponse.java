package com.testing.traningproject.model.dto.response;

import com.testing.traningproject.model.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Response DTO for Booking details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

    private Long id;
    private Long customerId;
    private String customerName;
    private Long serviceId;
    private String serviceTitle;
    private String providerName;
    private LocalDate slotDate;
    private String dayOfWeek; // MONDAY, TUESDAY, etc.
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal totalPrice;
    private BookingStatus status;
    private String cancellationReason;
    private LocalDateTime bookingDate;
    private LocalDateTime cancelledAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
}

