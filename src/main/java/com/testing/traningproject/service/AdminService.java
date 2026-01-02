package com.testing.traningproject.service;

import com.testing.traningproject.exception.BadRequestException;
import com.testing.traningproject.exception.ResourceNotFoundException;
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
import java.util.stream.Collectors;

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

    /**
     * Get all pending service provider registrations
     */
    @Transactional(readOnly = true)
    public List<PendingProviderResponse> getPendingProviders() {
        log.info("Fetching pending service provider registrations");

        List<User> pendingProviders = userRepository.findByAccountStatus(AccountStatus.PENDING_APPROVAL);

        return pendingProviders.stream()
                .map(this::convertToPendingProviderResponse)
                .collect(Collectors.toList());
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

        // TODO: Send notification to provider (ACCOUNT_APPROVED)
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

        // TODO: Send notification to provider (ACCOUNT_REJECTED) with admin notes
        log.info("Provider rejected successfully: {}", provider.getEmail());
    }

    /**
     * Get all pending refund requests
     */
    @Transactional(readOnly = true)
    public List<PendingRefundResponse> getPendingRefunds() {
        log.info("Fetching pending refund requests");

        List<Refund> pendingRefunds = refundRepository.findByStatus(RefundStatus.PENDING);

        return pendingRefunds.stream()
                .map(this::convertToPendingRefundResponse)
                .collect(Collectors.toList());
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

        // Create REFUND transaction
        com.testing.traningproject.model.entity.Transaction refundTransaction = com.testing.traningproject.model.entity.Transaction.builder()
                .user(refund.getBooking().getCustomer())
                .booking(refund.getBooking())
                .transactionType(com.testing.traningproject.model.enums.TransactionType.REFUND)
                .amount(refund.getRefundAmount())
                .paymentMethod("Refund to original payment method")
                .status(com.testing.traningproject.model.enums.TransactionStatus.SUCCESS) // TODO: Change to PENDING then SUCCESS after Stripe
                .paymentGatewayTransactionId("MOCK_REFUND_" + System.currentTimeMillis()) // TODO: Real Stripe ID
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

        // TODO: Send notification to customer (REFUND_APPROVED)

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

        // TODO: Send notification to customer (REFUND_REJECTED) with admin notes

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

        // Booking Statistics (will be 0 for now, will be implemented in Phase 3)
        long totalBookings = bookingRepository.count();

        // Refund Statistics (will be 0 for now, will be implemented in Phase 3)
        long totalRefunds = refundRepository.count();

        return AdminStatsResponse.builder()
                .totalUsers(totalUsers)
                .totalCustomers(totalCustomers)
                .totalProviders(totalProviders)
                .activeProviders(activeProviders)
                .pendingProviders(pendingProviders)
                .rejectedProviders(rejectedProviders)
                .totalBookings(totalBookings)
                .pendingBookings(0L) // TODO: Implement in Phase 3
                .confirmedBookings(0L) // TODO: Implement in Phase 3
                .completedBookings(0L) // TODO: Implement in Phase 3
                .cancelledBookings(0L) // TODO: Implement in Phase 3
                .totalRevenue(java.math.BigDecimal.ZERO) // TODO: Implement in Phase 3
                .pendingPayments(java.math.BigDecimal.ZERO) // TODO: Implement in Phase 3
                .completedPayments(java.math.BigDecimal.ZERO) // TODO: Implement in Phase 3
                .totalRefunds(totalRefunds)
                .pendingRefunds(0L) // TODO: Implement in Phase 3
                .approvedRefunds(0L) // TODO: Implement in Phase 3
                .rejectedRefunds(0L) // TODO: Implement in Phase 3
                .totalRefundAmount(java.math.BigDecimal.ZERO) // TODO: Implement in Phase 3
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

        return convertToSubscriptionPlanResponse(plan);
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

        return convertToSubscriptionPlanResponse(plan);
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

        return subscriptionPlanRepository.findAll()
                .stream()
                .map(this::convertToSubscriptionPlanResponse)
                .collect(Collectors.toList());
    }

    // ==================== methods for convert to Response shape ====================

    private com.testing.traningproject.model.dto.response.SubscriptionPlanResponse convertToSubscriptionPlanResponse(
            com.testing.traningproject.model.entity.SubscriptionPlan plan) {
        return com.testing.traningproject.model.dto.response.SubscriptionPlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .description(plan.getDescription())
                .price(plan.getPrice())
                .durationDays(plan.getDurationDays())
                .isActive(plan.getIsActive())
                .build();
    }

    private PendingProviderResponse convertToPendingProviderResponse(User user) {
        return PendingProviderResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .bio(user.getBio())
                .professionalTitle(user.getProfessionalTitle())
                .certificateUrl(user.getCertificateUrl())
                .accountStatus(user.getAccountStatus())
                .roles(user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .build();
    }


    private PendingRefundResponse convertToPendingRefundResponse(Refund refund) {
        var booking = refund.getBooking();
        var customer = booking.getCustomer();
        var service = booking.getService();
        var provider = service.getProvider();

        return PendingRefundResponse.builder()
                .id(refund.getId())
                .bookingId(booking.getId())
                .customerName(customer.getFirstName() + " " + customer.getLastName())
                .customerEmail(customer.getEmail())
                .serviceName(service.getTitle())
                .providerName(provider.getFirstName() + " " + provider.getLastName())
                .refundAmount(refund.getRefundAmount())
                .refundReason(refund.getRefundReason())
                .status(refund.getStatus())
                .requestedAt(refund.getRequestedAt())
                .bookingDate(booking.getBookingDate())
                .build();
    }
}

