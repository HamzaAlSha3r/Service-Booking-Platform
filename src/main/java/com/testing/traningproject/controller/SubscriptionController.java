package com.testing.traningproject.controller;

import com.testing.traningproject.model.dto.request.SubscribeRequest;
import com.testing.traningproject.model.dto.response.SubscriptionPlanResponse;
import com.testing.traningproject.model.dto.response.SubscriptionResponse;
import com.testing.traningproject.security.CustomUserDetails;
import com.testing.traningproject.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Subscription Controller
 */
@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * Get all available subscription plans
     */
    @GetMapping("/plans")
    public ResponseEntity<List<SubscriptionPlanResponse>> getAllPlans() {
        return ResponseEntity.ok(subscriptionService.getAllPlans());
    }

    /**
     * Subscribe to a plan
     * Requires SERVICE_PROVIDER role
     */
    @PostMapping("/subscribe")
    @PreAuthorize("hasRole('SERVICE_PROVIDER')")
    public ResponseEntity<SubscriptionResponse> subscribe(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SubscribeRequest request) {

        SubscriptionResponse response = subscriptionService.subscribe(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get current subscription
     * Requires SERVICE_PROVIDER role
     */
    @GetMapping("/my-subscription")
    @PreAuthorize("hasRole('SERVICE_PROVIDER')")
    public ResponseEntity<SubscriptionResponse> getCurrentSubscription(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok(subscriptionService.getCurrentSubscription(userDetails.getId()));
    }

    /**
     * Cancel auto-renewal
     * Requires SERVICE_PROVIDER role
     */
    @PutMapping("/cancel-renewal")
    @PreAuthorize("hasRole('SERVICE_PROVIDER')")
    public ResponseEntity<SubscriptionResponse> cancelAutoRenewal(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok(subscriptionService.cancelAutoRenewal(userDetails.getId()));
    }
}

