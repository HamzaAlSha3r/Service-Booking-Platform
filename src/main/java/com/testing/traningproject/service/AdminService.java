package com.testing.traningproject.service;

import com.testing.traningproject.exception.BadRequestException;
import com.testing.traningproject.exception.ResourceNotFoundException;
import com.testing.traningproject.mapper.AdminMapper;
import com.testing.traningproject.mapper.SubscriptionMapper;
import com.testing.traningproject.model.dto.request.CreateSubscriptionPlanRequest;
import com.testing.traningproject.model.dto.request.ProviderApprovalRequest;
import com.testing.traningproject.model.dto.request.RefundDecisionRequest;
import com.testing.traningproject.model.dto.request.UpdateSubscriptionPlanRequest;
import com.testing.traningproject.model.dto.response.AdminStatsResponse;
import com.testing.traningproject.model.dto.response.PendingProviderResponse;
import com.testing.traningproject.model.dto.response.PendingRefundResponse;
import com.testing.traningproject.model.dto.response.SubscriptionPlanResponse;
import com.testing.traningproject.model.entity.Refund;
import com.testing.traningproject.model.entity.SubscriptionPlan;
import com.testing.traningproject.model.entity.Transaction;
import com.testing.traningproject.model.entity.User;
import com.testing.traningproject.model.enums.AccountStatus;
import com.testing.traningproject.model.enums.RefundStatus;
import com.testing.traningproject.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for Admin operations
 * Handles provider approvals, refund decisions, and platform statistics
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final RefundRepository refundRepository;
    private final BookingRepository bookingRepository;
    private final ServiceRepository serviceRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationService notificationService;
    private final com.testing.traningproject.service.payment.PaymentStrategyFactory paymentStrategyFactory;
    private final AdminMapper adminMapper; // ✅ MapStruct mapper
    private final SubscriptionMapper subscriptionMapper; // ✅ MapStruct mapper

    /**
     * Get all pending service provider registrations
     */
    @Transactional(readOnly = true)
    public List<PendingProviderResponse> getPendingProviders() {
        log.info("Fetching pending service provider registrations");

        return adminMapper.toPendingProviderResponseList(
                userRepository.findByAccountStatus(AccountStatus.PENDING_APPROVAL)
        );
    }

    /**
     * Approve service provider registration
     */
    @Transactional
    public void approveProvider(Long providerId, ProviderApprovalRequest request) {
        User provider = userRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with ID: " + providerId));

        if (provider.getAccountStatus() != AccountStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Provider is not in pending approval status");
        }

        provider.setAccountStatus(AccountStatus.ACTIVE);
        provider.setUpdatedAt(LocalDateTime.now());
        userRepository.save(provider);

        // Send notification to provider (ACCOUNT_APPROVED)
        String notificationMessage = "Your service provider account has been approved! You can now subscribe to a plan and start offering services.";
        if (request != null && request.getAdminNotes() != null) {
            notificationMessage += "\n\nAdmin notes: " + request.getAdminNotes();
            log.info("Provider approved with notes: {}", request.getAdminNotes());
        }

        notificationService.createNotification(
            provider,
            com.testing.traningproject.model.enums.NotificationType.ACCOUNT_APPROVED,
            "Account Approved ✅",
            notificationMessage
        );

        log.info("Provider approved successfully: {}", provider.getEmail());
    }

    /**
     * Reject service provider registration
     */
    @Transactional
    public void rejectProvider(Long providerId, ProviderApprovalRequest request) {
        User provider = userRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with ID: " + providerId));

        if (provider.getAccountStatus() != AccountStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Provider is not in pending approval status");
        }

        provider.setAccountStatus(AccountStatus.REJECTED);
        provider.setUpdatedAt(LocalDateTime.now());
        userRepository.save(provider);

        String notificationMessage = "Your service provider registration has been rejected.";
        if (request != null && request.getAdminNotes() != null) {
            notificationMessage += "\n\nReason: " + request.getAdminNotes();
            log.info("Provider rejected with reason: {}", request.getAdminNotes());
        }

        notificationService.createNotification(
            provider,
            com.testing.traningproject.model.enums.NotificationType.ACCOUNT_REJECTED,
            "Account Rejected ❌",
            notificationMessage
        );

        log.info("Provider rejected successfully: {}", provider.getEmail());
    }

    /**
     * Get all pending refund requests
     */
    @Transactional(readOnly = true)
    public List<PendingRefundResponse> getPendingRefunds() {
        log.info("Fetching pending refund requests");

        return adminMapper.toPendingRefundResponseList(
                refundRepository.findByStatus(RefundStatus.PENDING)
        );
    }

    /**
     * Approve refund request
     */
    @Transactional
    public void approveRefund(Long refundId, RefundDecisionRequest request) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new ResourceNotFoundException("Refund not found with ID: " + refundId));

        if (refund.getStatus() != RefundStatus.PENDING) {
            throw new IllegalStateException("Refund is not in pending status");
        }

        refund.setStatus(RefundStatus.APPROVED);
        refund.setAdminNotes(request.getAdminNotes());
        refund.setProcessedAt(LocalDateTime.now());
        refund.setUpdatedAt(LocalDateTime.now());
        refundRepository.save(refund);
        log.info("Refund ID: {} approved by admin", refundId);

        // Get original payment transaction
        Transaction originalTransaction = transactionRepository.findByBookingIdAndTransactionType(
                refund.getBooking().getId(),
                com.testing.traningproject.model.enums.TransactionType.BOOKING_PAYMENT
            );

        String refundTransactionId;
        String paymentMethodUsed = "stripe"; // Default
        try {
            // Extract payment method from original transaction
            if (originalTransaction != null && originalTransaction.getPaymentGatewayTransactionId() != null) {
                if (originalTransaction.getPaymentGatewayTransactionId().startsWith("PAYPAL_")) {
                    paymentMethodUsed = "paypal";
                }
            }

            // Get payment strategy and process refund
            var paymentStrategy = paymentStrategyFactory.getStrategy(paymentMethodUsed);
            refundTransactionId = paymentStrategy.processRefund(
                originalTransaction != null ? originalTransaction.getPaymentGatewayTransactionId() : "N/A",
                refund.getRefundAmount()
            );

            log.info("Refund processed successfully via {} - Refund ID: {}",
                    paymentStrategy.getPaymentMethodName(), refundTransactionId);
        } catch (Exception e) {
            log.error("Refund processing failed: {}", e.getMessage());
            refundTransactionId = "REFUND_FAILED_" + System.currentTimeMillis();
        }

        // Create REFUND transaction
        Transaction refundTransaction = Transaction.builder()
                .user(refund.getBooking().getCustomer())
                .booking(refund.getBooking())
                .transactionType(com.testing.traningproject.model.enums.TransactionType.REFUND)
                .amount(refund.getRefundAmount())
                .paymentMethod("Refund to original payment method")
                .status(com.testing.traningproject.model.enums.TransactionStatus.SUCCESS)
                .paymentGatewayTransactionId(refundTransactionId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        transactionRepository.save(refundTransaction);
        log.info("REFUND transaction created with ID: {} - Amount: {}", refundTransaction.getId(), refundTransaction.getAmount());

        // Update refund with transaction ID
        refund.setTransaction(refundTransaction);
        refund.setStatus(com.testing.traningproject.model.enums.RefundStatus.COMPLETED);
        refund.setUpdatedAt(LocalDateTime.now());
        refundRepository.save(refund);
        log.info("Refund ID: {} marked as COMPLETED", refund.getId());

        // Send notification to customer (REFUND_APPROVED)
        notificationService.createNotification(
            refund.getBooking().getCustomer(),
            com.testing.traningproject.model.enums.NotificationType.REFUND_APPROVED,
            "Refund Approved ✅",
            "Your refund request for booking #" + refund.getBooking().getId() +
            " has been approved. Amount: $" + refund.getRefundAmount() +
            " will be refunded to your original payment method."
        );

        log.info("Refund approved successfully: {}", refundId);
    }

    /**
     * Reject refund request
     */
    @Transactional
    public void rejectRefund(Long refundId, RefundDecisionRequest request) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new ResourceNotFoundException("Refund not found with ID: " + refundId));

        if (refund.getStatus() != RefundStatus.PENDING) {
            throw new IllegalStateException("Refund is not in pending status");
        }

        refund.setStatus(RefundStatus.REJECTED);
        refund.setAdminNotes(request.getAdminNotes());
        refund.setProcessedAt(LocalDateTime.now());
        refund.setUpdatedAt(LocalDateTime.now());
        refundRepository.save(refund);

        // Send notification to customer (REFUND_REJECTED)
        String notificationMessage = "Your refund request for booking #" + refund.getBooking().getId() + " has been rejected.";
        if (request.getAdminNotes() != null) {
            notificationMessage += "\n\nReason: " + request.getAdminNotes();
        }

        notificationService.createNotification(
            refund.getBooking().getCustomer(),
            com.testing.traningproject.model.enums.NotificationType.REFUND_REJECTED,
            "Refund Rejected ❌",
            notificationMessage
        );

        log.info("Refund rejected successfully: {}", refundId);
    }

    /**
     * Get platform statistics for admin dashboard
     */
    @Transactional(readOnly = true)
    public AdminStatsResponse getPlatformStats() {
        log.info("Fetching platform statistics");

        // User Statistics
        long totalUsers = userRepository.count();

        // Count users by role (users can have multiple roles, so we count distinct users)
        long totalCustomers = userRepository.findAll().stream()
                .filter(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getName() == com.testing.traningproject.model.enums.RoleName.CUSTOMER))
                .count();

        long totalProviders = userRepository.findAll().stream()
                .filter(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getName() == com.testing.traningproject.model.enums.RoleName.SERVICE_PROVIDER))
                .count();

        // Provider statistics by status
        long activeProviders = userRepository.findByAccountStatus(com.testing.traningproject.model.enums.AccountStatus.ACTIVE)
                .stream()
                .filter(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getName() == com.testing.traningproject.model.enums.RoleName.SERVICE_PROVIDER))
                .count();

        long pendingProviders = userRepository.findByAccountStatus(com.testing.traningproject.model.enums.AccountStatus.PENDING_APPROVAL).size();

        long rejectedProviders = userRepository.findByAccountStatus(com.testing.traningproject.model.enums.AccountStatus.REJECTED).size();

        // Service Statistics
        long totalServices = serviceRepository.count();
        long activeServices = serviceRepository.findAll().stream()
                .filter(service -> service.getIsActive() != null && service.getIsActive())
                .count();

        // Category Statistics
        long totalCategories = categoryRepository.count();

        // Subscription Statistics
        long activeSubscriptions = subscriptionRepository.countByStatus(com.testing.traningproject.model.enums.SubscriptionStatus.ACTIVE);
        long expiredSubscriptions = subscriptionRepository.countByStatus(com.testing.traningproject.model.enums.SubscriptionStatus.EXPIRED);

        // Booking Statistics
        long totalBookings = bookingRepository.count();
        long pendingBookings = bookingRepository.countByStatus(com.testing.traningproject.model.enums.BookingStatus.PENDING);
        long confirmedBookings = bookingRepository.countByStatus(com.testing.traningproject.model.enums.BookingStatus.CONFIRMED);
        long completedBookings = bookingRepository.countByStatus(com.testing.traningproject.model.enums.BookingStatus.COMPLETED);
        long cancelledBookings = bookingRepository.countByStatus(com.testing.traningproject.model.enums.BookingStatus.CANCELLED);

        // Transaction Statistics (Revenue & Payments)
        java.math.BigDecimal totalRevenue = transactionRepository.sumSuccessfulTransactionsByType(
                com.testing.traningproject.model.enums.TransactionType.BOOKING_PAYMENT);
        if (totalRevenue == null) totalRevenue = java.math.BigDecimal.ZERO;

        java.math.BigDecimal pendingPayments = transactionRepository.sumPendingTransactionsByType(
                com.testing.traningproject.model.enums.TransactionType.BOOKING_PAYMENT);
        if (pendingPayments == null) pendingPayments = java.math.BigDecimal.ZERO;

        java.math.BigDecimal completedPayments = totalRevenue; // Same as total revenue (successful payments)

        // Refund Statistics
        long totalRefunds = refundRepository.count();
        long pendingRefunds = refundRepository.countByStatus(com.testing.traningproject.model.enums.RefundStatus.PENDING);
        long approvedRefunds = refundRepository.countByStatus(com.testing.traningproject.model.enums.RefundStatus.APPROVED);
        long rejectedRefunds = refundRepository.countByStatus(com.testing.traningproject.model.enums.RefundStatus.REJECTED);

        java.math.BigDecimal totalRefundAmount = refundRepository.sumRefundAmountByStatus(
                com.testing.traningproject.model.enums.RefundStatus.COMPLETED);
        if (totalRefundAmount == null) totalRefundAmount = java.math.BigDecimal.ZERO;

        return AdminStatsResponse.builder()
                .totalUsers(totalUsers)
                .totalCustomers(totalCustomers)
                .totalProviders(totalProviders)
                .activeProviders(activeProviders)
                .pendingProviders(pendingProviders)
                .rejectedProviders(rejectedProviders)
                .totalBookings(totalBookings)
                .pendingBookings(pendingBookings)
                .confirmedBookings(confirmedBookings)
                .completedBookings(completedBookings)
                .cancelledBookings(cancelledBookings)
                .totalRevenue(totalRevenue)
                .pendingPayments(pendingPayments)
                .completedPayments(completedPayments)
                .totalRefunds(totalRefunds)
                .pendingRefunds(pendingRefunds)
                .approvedRefunds(approvedRefunds)
                .rejectedRefunds(rejectedRefunds)
                .totalRefundAmount(totalRefundAmount)
                .totalServices(totalServices)
                .activeServices(activeServices)
                .totalCategories(totalCategories)
                .activeSubscriptions(activeSubscriptions)
                .expiredSubscriptions(expiredSubscriptions)
                .build();
    }

    // ==================== Subscription Plan Management ====================

    /**
     * Create new subscription plan
     */
    @Transactional
    public SubscriptionPlanResponse createSubscriptionPlan(CreateSubscriptionPlanRequest request) {

        // Check if plan with same name exists
        if (subscriptionPlanRepository.existsByName(request.getName())) {
            throw new com.testing.traningproject.exception.DuplicateResourceException(
                    "Subscription plan with name '" + request.getName() + "' already exists");
        }

        // save the Subscription Plan in entity
        SubscriptionPlan plan = SubscriptionPlan.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .durationDays(request.getDurationDays())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        plan = subscriptionPlanRepository.save(plan);

        log.info("Subscription plan created successfully - ID: {}", plan.getId());

        return subscriptionMapper.toPlanResponse(plan);
    }

    /**
     * Update subscription plan
     */
    @Transactional
    public SubscriptionPlanResponse updateSubscriptionPlan(Integer planId, UpdateSubscriptionPlanRequest request) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found with ID: " + planId));

        if (request.getName() != null) {
            plan.setName(request.getName());
        }
        if (request.getDescription() != null) {
            plan.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            plan.setPrice(request.getPrice());
        }
        if (request.getDurationDays() != null) {
            plan.setDurationDays(request.getDurationDays());
        }
        if (request.getIsActive() != null) {
            plan.setIsActive(request.getIsActive());
        }

        plan.setUpdatedAt(LocalDateTime.now());
        plan = subscriptionPlanRepository.save(plan);

        log.info("Subscription plan updated successfully - ID: {}", planId);

        return subscriptionMapper.toPlanResponse(plan);
    }

    /**
     * Delete subscription plan
     */
    @Transactional
    public void deleteSubscriptionPlan(Integer planId) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found with ID: " + planId));

        // Check if plan has active subscriptions from service provider
        long activeSubscriptions = subscriptionRepository.countByPlanIdAndStatus(
                planId, com.testing.traningproject.model.enums.SubscriptionStatus.ACTIVE);

        // can't delete this plan because there are active subscriptions
        if (activeSubscriptions > 0) {
            throw new BadRequestException(
                    "Cannot delete subscription plan with active subscriptions. Deactivate it instead.");
        }

        subscriptionPlanRepository.delete(plan);

        log.info("Subscription plan deleted successfully - ID: {}", planId);
    }

    /**
     * Get all subscription plans (for admin management)
     */
    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> getAllSubscriptionPlans() {
        log.info("Fetching all subscription plans for admin");

        return subscriptionMapper.toPlanResponseList(subscriptionPlanRepository.findAll());
    }
}

