package com.testing.traningproject.controller;

import com.testing.traningproject.model.dto.request.CreateServiceRequest;
import com.testing.traningproject.model.dto.request.SetAvailabilityRequest;
import com.testing.traningproject.model.dto.request.UpdateServiceRequest;
import com.testing.traningproject.model.dto.response.BookingResponse;
import com.testing.traningproject.model.dto.response.ProviderAvailabilityResponse;
import com.testing.traningproject.model.dto.response.ReviewResponse;
import com.testing.traningproject.model.dto.response.ServiceResponse;
import com.testing.traningproject.security.CustomUserDetails;
import com.testing.traningproject.service.BookingService;
import com.testing.traningproject.service.ProviderService;
import com.testing.traningproject.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Provider Controller
 */
@RestController
@RequestMapping("/api/provider")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SERVICE_PROVIDER')")
public class ProviderController {

    private final ProviderService providerService;
    private final BookingService bookingService;
    private final ReviewService reviewService;

    /**
     * Create a new service
     */
    @PostMapping("/services")
    public ResponseEntity<ServiceResponse> createService(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateServiceRequest request) {

        ServiceResponse response = providerService.createService(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update existing service
     */
    @PutMapping("/services/{id}")
    public ResponseEntity<ServiceResponse> updateService(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody UpdateServiceRequest request) {

        return ResponseEntity.ok(providerService.updateService(userDetails.getId(), id, request));
    }

    /**
     * Delete service
     */
    @DeleteMapping("/services/{id}")
    public ResponseEntity<Void> deleteService(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {

        providerService.deleteService(userDetails.getId(), id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get provider's services
     */
    @GetMapping("/services")
    public ResponseEntity<List<ServiceResponse>> getMyServices(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok(providerService.getProviderServices(userDetails.getId()));
    }

    // ==================== Availability Management ====================

    /**
     * Set availability
     */
    @PostMapping("/availability")
    public ResponseEntity<ProviderAvailabilityResponse> setAvailability(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SetAvailabilityRequest request) {

        ProviderAvailabilityResponse response = providerService.setAvailability(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get provider's availability
     */
    @GetMapping("/availability")
    public ResponseEntity<List<ProviderAvailabilityResponse>> getMyAvailability(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok(providerService.getProviderAvailability(userDetails.getId()));
    }

    /**
     * Delete availability
     */
    @DeleteMapping("/availability/{id}")
    public ResponseEntity<Void> deleteAvailability(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {

        providerService.deleteAvailability(userDetails.getId(), id);
        return ResponseEntity.noContent().build();
    }

    // ==================== Booking Management ====================

    /**
     * Get all bookings for the provider
     */
    @GetMapping("/bookings")
    public ResponseEntity<List<BookingResponse>> getMyBookings(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok(bookingService.getProviderBookings(userDetails.getId()));
    }

    /**
     * Mark booking as completed (triggers PAYOUT transaction)
     */
    @PutMapping("/bookings/{id}/complete")
    public ResponseEntity<BookingResponse> completeBooking(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {

        BookingResponse response = bookingService.completeBooking(userDetails.getId(), id);
        return ResponseEntity.ok(response);
    }

    // ==================== Review Management ====================

    /**
     * Get all reviews for the provider
     */
    @GetMapping("/reviews")
    public ResponseEntity<List<ReviewResponse>> getMyReviews(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok(reviewService.getProviderReviews(userDetails.getId()));
    }
}

