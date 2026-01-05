package com.testing.traningproject.controller;

import com.testing.traningproject.model.dto.request.CancelBookingRequest;
import com.testing.traningproject.model.dto.request.CreateBookingRequest;
import com.testing.traningproject.model.dto.request.CreateReviewRequest;
import com.testing.traningproject.model.dto.response.BookingResponse;
import com.testing.traningproject.model.dto.response.RefundResponse;
import com.testing.traningproject.model.dto.response.ReviewResponse;
import com.testing.traningproject.model.dto.response.TimeSlotResponse;
import com.testing.traningproject.model.dto.response.TransactionResponse;
import com.testing.traningproject.security.CustomUserDetails;
import com.testing.traningproject.service.BookingService;
import com.testing.traningproject.service.RefundService;
import com.testing.traningproject.service.ReviewService;
import com.testing.traningproject.service.TimeSlotService;
import com.testing.traningproject.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Customer operations
 * Handles bookings, reviews, and transaction history for customers
 */
@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final BookingService bookingService;
    private final ReviewService reviewService;
    private final TransactionService transactionService;
    private final TimeSlotService timeSlotService;
    private final RefundService refundService;

    // ==================== Time Slot Endpoints ====================

    /**
     * Get available time slots for a service
     * Optional parameters:
     * - fromDate: Start date (default: today)
     * - toDate: End date (default: +30 days from fromDate)
     * - limit: Max number of slots to return (default: 50)
     */
    @GetMapping("/services/{serviceId}/slots")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<TimeSlotResponse>> getAvailableTimeSlots(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long serviceId,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(defaultValue = "50") int limit) {

        log.info("Customer ID: {} fetching available time slots for service ID: {} (fromDate: {}, toDate: {}, limit: {})",
                userDetails.getId(), serviceId, fromDate, toDate, limit);

        List<TimeSlotResponse> slots = timeSlotService.getAvailableTimeSlotsForService(
                serviceId, fromDate, toDate, limit);

        return ResponseEntity.ok(slots);
    }

    // ==================== Booking Endpoints ====================

    /**
     * Create a new booking
     */
    @PostMapping("/bookings")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<BookingResponse> createBooking(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateBookingRequest request) {

        log.info("Customer ID: {} creating booking for service ID: {}", userDetails.getId(), request.getServiceId());

        BookingResponse response = bookingService.createBooking(userDetails.getId(), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all bookings for the authenticated customer
     */
    @GetMapping("/bookings")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<BookingResponse>> getMyBookings(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Fetching all bookings for customer ID: {}", userDetails.getId());

        List<BookingResponse> bookings = bookingService.getMyBookings(userDetails.getId());

        return ResponseEntity.ok(bookings);
    }

    /**
     * Get booking by ID
     */
    @GetMapping("/bookings/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<BookingResponse> getBookingById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {

        log.info("Customer ID: {} fetching booking ID: {}", userDetails.getId(), id);

        BookingResponse booking = bookingService.getBookingById(userDetails.getId(), id);

        return ResponseEntity.ok(booking);
    }

    /**
     * Cancel a booking
     */
    @PutMapping("/bookings/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<BookingResponse> cancelBooking(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody CancelBookingRequest request) {

        log.info("Customer ID: {} cancelling booking ID: {}", userDetails.getId(), id);

        BookingResponse response = bookingService.cancelBooking(userDetails.getId(), id, request);

        return ResponseEntity.ok(response);
    }

    // ==================== Review Endpoints ====================

    /**
     * Submit a review for a completed booking
     */
    @PostMapping("/reviews")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ReviewResponse> createReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateReviewRequest request) {

        log.info("Customer ID: {} submitting review for booking ID: {}", userDetails.getId(), request.getBookingId());

        ReviewResponse response = reviewService.createReview(userDetails.getId(), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ==================== Transaction Endpoints ====================

    /**
     * Get all transactions for the authenticated customer
     */
    @GetMapping("/transactions")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<TransactionResponse>> getMyTransactions(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Fetching all transactions for customer ID: {}", userDetails.getId());

        List<TransactionResponse> transactions = transactionService.getMyTransactions(userDetails.getId());

        return ResponseEntity.ok(transactions);
    }

    // ==================== Refund Endpoints ====================

    /**
     * Get all refunds for the authenticated customer
     * Shows refund status (PENDING, APPROVED, REJECTED, COMPLETED)
     */
    @GetMapping("/refunds")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<RefundResponse>> getMyRefunds(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Fetching all refunds for customer ID: {}", userDetails.getId());

        List<RefundResponse> refunds = refundService.getCustomerRefunds(userDetails.getId());

        return ResponseEntity.ok(refunds);
    }
}

