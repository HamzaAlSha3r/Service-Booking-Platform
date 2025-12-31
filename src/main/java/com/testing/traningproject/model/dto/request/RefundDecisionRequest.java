package com.testing.traningproject.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for refund decision
 * Used by admin to approve or reject refund request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundDecisionRequest {

    /**
     * Admin notes explaining the decision
     * Required for rejection, optional for approval
     */
    private String adminNotes;
}

