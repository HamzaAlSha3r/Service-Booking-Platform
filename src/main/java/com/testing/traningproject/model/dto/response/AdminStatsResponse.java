package com.testing.traningproject.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for platform statistics
 * Returns overall platform metrics for admin dashboard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsResponse {

    // User Statistics
    private Long totalUsers;
    private Long totalCustomers;
    private Long totalProviders;
    private Long activeProviders;
    private Long pendingProviders;
    private Long rejectedProviders;

    // Booking Statistics
    private Long totalBookings;
    private Long pendingBookings;
    private Long confirmedBookings;
    private Long completedBookings;
    private Long cancelledBookings;

    // Financial Statistics
    private BigDecimal totalRevenue;
    private BigDecimal pendingPayments;
    private BigDecimal completedPayments;

    // Refund Statistics
    private Long totalRefunds;
    private Long pendingRefunds;
    private Long approvedRefunds;
    private Long rejectedRefunds;
    private BigDecimal totalRefundAmount;

    // Service Statistics
    private Long totalServices;
    private Long activeServices;
    private Long totalCategories;

    // Subscription Statistics
    private Long activeSubscriptions;
    private Long expiredSubscriptions;
}

