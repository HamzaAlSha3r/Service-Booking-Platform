package com.testing.traningproject.service;

import com.testing.traningproject.exception.BadRequestException;
import com.testing.traningproject.exception.ForbiddenException;
import com.testing.traningproject.exception.ResourceNotFoundException;
import com.testing.traningproject.model.dto.request.SubscribeRequest;
import com.testing.traningproject.model.dto.response.SubscriptionPlanResponse;
import com.testing.traningproject.model.dto.response.SubscriptionResponse;
import com.testing.traningproject.model.entity.*;
import com.testing.traningproject.model.enums.AccountStatus;
import com.testing.traningproject.model.enums.RoleName;
import com.testing.traningproject.model.enums.SubscriptionStatus;
import com.testing.traningproject.model.enums.TransactionStatus;
import com.testing.traningproject.repository.SubscriptionPlanRepository;
import com.testing.traningproject.repository.SubscriptionRepository;
import com.testing.traningproject.repository.TransactionRepository;
import com.testing.traningproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Subscription Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Get all available subscription plans
     */
    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> getAllPlans() {
        log.info("Fetching all active subscription plans");

        return subscriptionPlanRepository.findByIsActiveTrue()
                .stream()
                .map(this::convertToPlanResponse)
                .collect(Collectors.toList());
    }

    /**
     * Subscribe to a plan
     */
    @Transactional
    public SubscriptionResponse subscribe(Long userId, SubscribeRequest request) {
        log.info("Processing subscription request for user ID: {}", userId);

        // Validate user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if user is SERVICE_PROVIDER
        boolean isProvider = user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleName.SERVICE_PROVIDER);

        if (!isProvider) {
            throw new ForbiddenException("Only service providers can subscribe to plans");
        }

        // Check if user is approved
        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new ForbiddenException("Your account must be approved by admin before subscribing");
        }

        // Check if user already has active subscription
        subscriptionRepository.findActiveSubscriptionByProviderId(userId)
                .ifPresent(sub -> {
                    throw new BadRequestException("You already have an active subscription");
                });

        // Get subscription plan
        SubscriptionPlan plan = subscriptionPlanRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found"));

        if (!plan.getIsActive()) {
            throw new BadRequestException("This subscription plan is not available");
        }

        // 6. Create subscription
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(plan.getDurationDays());

        Subscription subscription = Subscription.builder()
                .provider(user)
                .plan(plan)
                .startDate(startDate)
                .endDate(endDate)
                .status(SubscriptionStatus.ACTIVE)
                .autoRenew(request.getAutoRenew())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        subscription = subscriptionRepository.save(subscription);

        // Process payment
        Transaction transaction = Transaction.builder()
                .user(user)
                .subscription(subscription)
                .transactionType(com.testing.traningproject.model.enums.TransactionType.SUBSCRIPTION_PAYMENT)
                .amount(plan.getPrice())
                .paymentMethod(request.getPaymentMethod())
                .status(TransactionStatus.SUCCESS)
                .updatedAt(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);

        log.info("Subscription created successfully for user ID: {} - Subscription ID: {}", userId, subscription.getId());

        return convertToSubscriptionResponse(subscription);
    }

    /**
     * Get current user subscription
     */
    @Transactional(readOnly = true)
    public SubscriptionResponse getCurrentSubscription(Long userId) {
        log.info("Fetching current subscription for user ID: {}", userId);

        Subscription subscription = subscriptionRepository.findActiveSubscriptionByProviderId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No active subscription found"));

        return convertToSubscriptionResponse(subscription);
    }

    /**
     * Cancel auto-renewal
     */
    @Transactional
    public SubscriptionResponse cancelAutoRenewal(Long userId) {
        Subscription subscription = subscriptionRepository.findActiveSubscriptionByProviderId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No active subscription found"));

        subscription.setAutoRenew(false);
        subscription.setUpdatedAt(LocalDateTime.now());
        subscription = subscriptionRepository.save(subscription);

        log.info("Auto-renewal cancelled for subscription ID: {}", subscription.getId());

        return convertToSubscriptionResponse(subscription);
    }

    /**
     * Check if user has active subscription
     */
    @Transactional(readOnly = true)
    public boolean hasActiveSubscription(Long userId) {
        return subscriptionRepository.findActiveSubscriptionByProviderId(userId).isPresent();
    }

    // ==================== methods to convert for Response shape ====================

    private SubscriptionPlanResponse convertToPlanResponse(SubscriptionPlan plan) {
        return SubscriptionPlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .description(plan.getDescription())
                .price(plan.getPrice())
                .durationDays(plan.getDurationDays())
                .isActive(plan.getIsActive())
                .build();
    }

    private SubscriptionResponse convertToSubscriptionResponse(Subscription subscription) {
        long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), subscription.getEndDate());

        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .providerId(subscription.getProvider().getId())
                .providerName(subscription.getProvider().getFirstName() + " " + subscription.getProvider().getLastName())
                .providerEmail(subscription.getProvider().getEmail())
                .planId(subscription.getPlan().getId())
                .planName(subscription.getPlan().getName())
                .planPrice(subscription.getPlan().getPrice())
                .planDurationDays(subscription.getPlan().getDurationDays())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .status(subscription.getStatus().name())
                .autoRenew(subscription.getAutoRenew())
                .daysRemaining(daysRemaining > 0 ? daysRemaining : 0)
                .createdAt(subscription.getCreatedAt())
                .updatedAt(subscription.getUpdatedAt())
                .build();
    }
}

