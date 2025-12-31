package com.testing.traningproject.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for provider approval decision
 * Used by admin to approve or reject service provider registration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderApprovalRequest {

    /**
     * Admin notes explaining the decision
     * Required for rejection, optional for approval
     */
    private String adminNotes;
}

