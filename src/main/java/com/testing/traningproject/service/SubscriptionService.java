package com.testing.traningproject.service;

import com.testing.traningproject.exception.BadRequestException;
import com.testing.traningproject.exception.ForbiddenException;
import com.testing.traningproject.exception.ResourceNotFoundException;
import com.testing.traningproject.mapper.SubscriptionMapper;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
    private final NotificationService notificationService;
    private final PaymentService paymentService;
    private final SubscriptionMapper subscriptionMapper; // ✅ MapStruct mapper

    /**
     * Get all available subscription plans
     */
    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> getAllPlans() {
        log.info("Fetching all active subscription plans");

        return subscriptionMapper.toPlanResponseList(subscriptionPlanRepository.findByIsActiveTrue());
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

        // Create subscription
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(plan.getDurationDays());

        Subscription subscription = Subscription.builder()
                .provider(user)
                .plan(plan)
                .startDate(startDate)
                .endDate(endDate)
                .status(SubscriptionStatus.ACTIVE)
                .autoRenew(request.getAutoRenew() != null ? request.getAutoRenew() : true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        subscription = subscriptionRepository.save(subscription);

        // Process payment via PaymentService
        String paymentTransactionId;
        try {
            // Validate and process payment
            paymentTransactionId = paymentService.processPayment(
                request.getPaymentCard(),
                plan.getPrice(),
                "Subscription payment for: " + plan.getName()
            );
            log.info("Subscription payment processed successfully - Gateway TXN ID: {}", paymentTransactionId);
        } catch (Exception e) {
            // Payment failed - delete subscription and throw error
            subscriptionRepository.delete(subscription);
            log.error("Subscription payment failed - Subscription deleted: {}", e.getMessage());
            throw new BadRequestException("Payment failed: " + e.getMessage());
        }

        Transaction transaction = Transaction.builder()
                .user(user)
                .subscription(subscription)
                .transactionType(com.testing.traningproject.model.enums.TransactionType.SUBSCRIPTION_PAYMENT)
                .amount(plan.getPrice())
                .paymentMethod(request.getPaymentMethod() + " - " +
                              paymentService.maskCardNumber(request.getPaymentCard().getCardNumber()))
                .status(TransactionStatus.SUCCESS)
                .paymentGatewayTransactionId(paymentTransactionId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);

        log.info("Subscription created successfully for user ID: {} - Subscription ID: {}", userId, subscription.getId());

        // Send PAYMENT_SUCCESS notification
        notificationService.createNotification(
            user,
            com.testing.traningproject.model.enums.NotificationType.PAYMENT_SUCCESS,
            "Subscription Activated ✅",
            "Your subscription to '" + plan.getName() + "' has been activated successfully! " +
            "Valid until " + endDate + ". You can now create and manage services."
        );

        return subscriptionMapper.toResponse(subscription);
    }

    /**
     * Get current user subscription
     */
    @Transactional(readOnly = true)
    public SubscriptionResponse getCurrentSubscription(Long userId) {
        log.info("Fetching current subscription for user ID: {}", userId);

        Subscription subscription = subscriptionRepository.findActiveSubscriptionByProviderId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No active subscription found"));

        return subscriptionMapper.toResponse(subscription);
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

        return subscriptionMapper.toResponse(subscription);
    }

    /**
     * Check if user has active subscription
     */
    @Transactional(readOnly = true)
    public boolean hasActiveSubscription(Long userId) {
        return subscriptionRepository.findActiveSubscriptionByProviderId(userId).isPresent();
    }
}

