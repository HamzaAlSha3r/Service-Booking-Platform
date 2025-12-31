package com.testing.traningproject.service;

import com.testing.traningproject.exception.ResourceNotFoundException;
import com.testing.traningproject.model.dto.request.ProviderApprovalRequest;
import com.testing.traningproject.model.dto.request.RefundDecisionRequest;
import com.testing.traningproject.model.dto.response.AdminStatsResponse;
import com.testing.traningproject.model.dto.response.PendingProviderResponse;
import com.testing.traningproject.model.dto.response.PendingRefundResponse;
import com.testing.traningproject.model.entity.Refund;
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
        log.info("Approving provider with ID: {}", providerId);

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
        log.info("Rejecting provider with ID: {}", providerId);

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
        log.info("Approving refund with ID: {}", refundId);

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

        // TODO: Process actual refund via Stripe
        // TODO: Update transaction status to REFUNDED
        // TODO: Send notification to customer (REFUND_APPROVED)

        log.info("Refund approved successfully: {}", refundId);
    }

    /**
     * Reject refund request
     */
    @Transactional
    public void rejectRefund(Long refundId, RefundDecisionRequest request) {
        log.info("Rejecting refund with ID: {}", refundId);

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

    /**
     * Convert User entity to PendingProviderResponse DTO
     */
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

    /**
     * Convert Refund entity to PendingRefundResponse DTO
     */
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

