package com.testing.traningproject.controller;

import com.testing.traningproject.model.dto.request.ProviderApprovalRequest;
import com.testing.traningproject.model.dto.request.RefundDecisionRequest;
import com.testing.traningproject.model.dto.response.AdminStatsResponse;
import com.testing.traningproject.model.dto.response.PendingProviderResponse;
import com.testing.traningproject.model.dto.response.PendingRefundResponse;
import com.testing.traningproject.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    /**
     * Get all pending service provider registrations
     * GET /api/admin/providers/pending
     */
    // here dont need validation this as list check from admin
    @GetMapping("/providers/pending")
    public ResponseEntity<List<PendingProviderResponse>> getPendingProviders() {
        log.info("Admin: Fetching pending provider registrations");
        List<PendingProviderResponse> providers = adminService.getPendingProviders();
        return ResponseEntity.ok(providers);
    }

    /**
     * Approve service provider registration
     * PUT /api/admin/providers/{id}/approve
     */
    @PutMapping("/providers/{id}/approve")
    public ResponseEntity<String> approveProvider(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) ProviderApprovalRequest request) {
        log.info("Admin: Approving provider with ID: {}", id);

        if (request == null) {
            request = new ProviderApprovalRequest();
        }

        adminService.approveProvider(id, request);
        return ResponseEntity.ok("Provider approved successfully");
    }

    /**
     * Reject service provider registration
     * PUT /api/admin/providers/{id}/reject
     */
    @PutMapping("/providers/{id}/reject")
    public ResponseEntity<String> rejectProvider(
            @PathVariable Long id,
            @Valid @RequestBody ProviderApprovalRequest request) {
        log.info("Admin: Rejecting provider with ID: {}", id);
        adminService.rejectProvider(id, request);
        return ResponseEntity.ok("Provider rejected successfully");
    }

    /**
     * Get all pending refund requests
     * GET /api/admin/refunds/pending
     */
    @GetMapping("/refunds/pending")
    public ResponseEntity<List<PendingRefundResponse>> getPendingRefunds() {
        log.info("Admin: Fetching pending refund requests");
        List<PendingRefundResponse> refunds = adminService.getPendingRefunds();
        return ResponseEntity.ok(refunds);
    }

    /**
     * Approve refund request
     * PUT /api/admin/refunds/{id}/approve
     */
    @PutMapping("/refunds/{id}/approve")
    public ResponseEntity<String> approveRefund(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) RefundDecisionRequest request) {
        log.info("Admin: Approving refund with ID: {}", id);

        if (request == null) {
            request = new RefundDecisionRequest();
        }

        adminService.approveRefund(id, request);
        return ResponseEntity.ok("Refund approved successfully");
    }

    /**
     * Reject refund request
     * PUT /api/admin/refunds/{id}/reject
     */
    @PutMapping("/refunds/{id}/reject")
    public ResponseEntity<String> rejectRefund(
            @PathVariable Long id,
            @Valid @RequestBody RefundDecisionRequest request) {
        log.info("Admin: Rejecting refund with ID: {}", id);
        adminService.rejectRefund(id, request);
        return ResponseEntity.ok("Refund rejected successfully");
    }

    /**
     * Get platform statistics
     * GET /api/admin/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getPlatformStats() {
        log.info("Admin: Fetching platform statistics");
        AdminStatsResponse stats = adminService.getPlatformStats();
        return ResponseEntity.ok(stats);
    }
}

