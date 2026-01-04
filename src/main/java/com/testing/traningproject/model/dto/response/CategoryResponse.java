package com.testing.traningproject.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {
    private Integer id;
    private String name;
    private String description;
    private String iconUrl;
    private Boolean isActive;
    private Long totalServices;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

