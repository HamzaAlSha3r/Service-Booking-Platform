package com.testing.traningproject.model.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for updating a service
 * يُستخدم عند تحديث خدمة موجودة
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateServiceRequest {

    private Integer categoryId;

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private String description;

    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer durationMinutes;

    @Pattern(regexp = "ONLINE|IN_PERSON|BOTH", message = "Service type must be ONLINE, IN_PERSON, or BOTH")
    private String serviceType;

    private String locationAddress;

    private Boolean isActive;
}

