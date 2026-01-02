package com.testing.traningproject.model.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for creating a service
 * يُستخدم عند إنشاء خدمة جديدة من قبل Provider
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateServiceRequest {

    @NotNull(message = "Category ID is required")
    private Integer categoryId;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer durationMinutes;

    @NotBlank(message = "Service type is required")
    @Pattern(regexp = "ONLINE|IN_PERSON|BOTH", message = "Service type must be ONLINE, IN_PERSON, or BOTH")
    private String serviceType;

    private String locationAddress; // Required only if serviceType is IN_PERSON or BOTH
}

