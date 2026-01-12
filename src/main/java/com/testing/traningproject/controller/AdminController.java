package com.testing.traningproject.controller;

import com.testing.traningproject.model.dto.request.CategoryRequest;
import com.testing.traningproject.model.dto.request.CreateSubscriptionPlanRequest;
import com.testing.traningproject.model.dto.request.ProviderApprovalRequest;
import com.testing.traningproject.model.dto.request.RefundDecisionRequest;
import com.testing.traningproject.model.dto.request.UpdateSubscriptionPlanRequest;
import com.testing.traningproject.model.dto.response.*;
import com.testing.traningproject.service.AdminService;
import com.testing.traningproject.service.CategoryService;
import com.testing.traningproject.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller for Admin operations
 * All endpoints require ADMIN role
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final TransactionService transactionService;
    private final CategoryService categoryService;

    /**
     * Get all pending service provider registrations
     */
    @GetMapping("/providers/pending")
    public ResponseEntity<List<PendingProviderResponse>> getPendingProviders() {
        List<PendingProviderResponse> providers = adminService.getPendingProviders();
        return ResponseEntity.ok(providers);
    }

    //  Get all services with their bookings
    @GetMapping("/services/all-bookings")
    public ResponseEntity<List<ServiceWithBookingsResponse>> getAllServicesWithBookings() {
        List<ServiceWithBookingsResponse> services = adminService.getAllServicesWithBookings();
        return ResponseEntity.ok(services);
    }

    /**
     * Approve service provider registration
     */
    @PutMapping("/providers/{id}/approve")
    public ResponseEntity<String> approveProvider(@PathVariable Long id,
            @Valid @RequestBody(required = false) ProviderApprovalRequest request) {

        // if request null we create an empty ProviderApprovalRequest() to avoid errors
        if (request == null) {
            request = new ProviderApprovalRequest();
        }

        adminService.approveProvider(id, request);
        return ResponseEntity.ok("Provider approved successfully");
    }

    /**
     * Reject service provider registration
     */
    @PutMapping("/providers/{id}/reject")
    public ResponseEntity<String> rejectProvider(@PathVariable Long id,
            @Valid @RequestBody ProviderApprovalRequest request) {
        adminService.rejectProvider(id, request);
        return ResponseEntity.ok("Provider rejected successfully");
    }

    /**
     * Get all pending refund requests
     */
    @GetMapping("/refunds/pending")
    public ResponseEntity<List<PendingRefundResponse>> getPendingRefunds() {
        log.info("Admin: Fetching pending refund requests");
        List<PendingRefundResponse> refunds = adminService.getPendingRefunds();
        return ResponseEntity.ok(refunds);
    }

    /**
     * Approve refund request
     */
    @PutMapping("/refunds/{id}/approve")
    public ResponseEntity<String> approveRefund(@PathVariable Long id,
            @Valid @RequestBody(required = false) RefundDecisionRequest request) {

        // if request null we create an empty ProviderApprovalRequest() to avoid errors
        if (request == null) {
            request = new RefundDecisionRequest();
        }

        adminService.approveRefund(id, request);
        return ResponseEntity.ok("Refund approved successfully");
    }

    /**
     * Reject refund request
     */
    @PutMapping("/refunds/{id}/reject")
    public ResponseEntity<String> rejectRefund(@PathVariable Long id,
            @Valid @RequestBody RefundDecisionRequest request) {
        adminService.rejectRefund(id, request);
        return ResponseEntity.ok("Refund rejected successfully");
    }

    /**
     * Get platform statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getPlatformStats() {
        log.info("Admin: Fetching platform statistics");
        AdminStatsResponse stats = adminService.getPlatformStats();
        return ResponseEntity.ok(stats);
    }

    // ==================== Subscription Plan Management ====================

    /**
     * Create new subscription plan
     */
    @PostMapping("/subscription-plans")
    public ResponseEntity<SubscriptionPlanResponse> createSubscriptionPlan(
            @Valid @RequestBody CreateSubscriptionPlanRequest request) {
        log.info("Admin: Creating new subscription plan: {}", request.getName());

        SubscriptionPlanResponse response = adminService.createSubscriptionPlan(request);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(response);
    }

    /**
     * Update subscription plan
     */
    @PutMapping("/subscription-plans/{id}")
    public ResponseEntity<SubscriptionPlanResponse> updateSubscriptionPlan(@PathVariable Integer id,
            @Valid @RequestBody UpdateSubscriptionPlanRequest request) {
        log.info("Admin: Updating subscription plan ID: {}", id);
        SubscriptionPlanResponse response = adminService.updateSubscriptionPlan(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete subscription plan
     */
    @DeleteMapping("/subscription-plans/{id}")
    public ResponseEntity<String> deleteSubscriptionPlan(@PathVariable Integer id) {
        adminService.deleteSubscriptionPlan(id);
        return ResponseEntity.ok("Subscription plan deleted successfully");
    }

    /**
     * Get all subscription plans (for admin management)
     */
    @GetMapping("/subscription-plans")
    public ResponseEntity<List<SubscriptionPlanResponse>> getAllSubscriptionPlans() {
        log.info("Admin: Fetching all subscription plans");
        List<com.testing.traningproject.model.dto.response.SubscriptionPlanResponse> plans = adminService.getAllSubscriptionPlans();
        return ResponseEntity.ok(plans);
    }

    // ==================== Category Management ====================

    /**
     * Get all categories (includes inactive)
     */
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        log.info("Admin: Fetching all categories");
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * Create new category
     */
    @PostMapping("/categories")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        log.info("Admin: Creating new category: {}", request.getName());
        CategoryResponse category = categoryService.createCategory(request);
        return ResponseEntity.ok(category);
    }

    /**
     * Update existing category
     */
    @PutMapping("/categories/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable Integer id,
                                                            @Valid @RequestBody CategoryRequest request) {
        log.info("Admin: Updating category ID: {}", id);
        CategoryResponse category = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(category);
    }

    /**
     * Delete category (only if no services)
     */
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable Integer id) {
        log.info("Admin: Deleting category ID: {}", id);
        categoryService.deleteCategory(id);
        return ResponseEntity.ok("Category deleted successfully");
    }

    // ==================== Transaction Management ====================

    /**
     * Get all transactions (BOOKING_PAYMENT, SUBSCRIPTION_PAYMENT, REFUND, PAYOUT)
     */
    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionResponse>> getAllTransactions() {
        log.info("Admin: Fetching all transactions");
        List<TransactionResponse> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }
}
