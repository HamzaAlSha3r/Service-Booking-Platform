package com.testing.traningproject.service;

import com.testing.traningproject.exception.ResourceNotFoundException;
import com.testing.traningproject.model.dto.response.ServiceResponse;
import com.testing.traningproject.model.entity.Category;
import com.testing.traningproject.model.entity.Service;
import com.testing.traningproject.repository.CategoryRepository;
import com.testing.traningproject.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for public service browsing and search
 * No authentication required
 */
@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Slf4j
public class PublicServiceService {

    private final ServiceRepository serviceRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Search and filter services with multiple criteria
     */
    @Transactional(readOnly = true)
    public List<ServiceResponse> searchServices(Integer categoryId, String categoryName,
                                                  String search, BigDecimal minPrice,
                                                  BigDecimal maxPrice, String sortBy) {
        log.info("Searching services - categoryId: {}, categoryName: {}, search: {}, minPrice: {}, maxPrice: {}, sortBy: {}",
                categoryId, categoryName, search, minPrice, maxPrice, sortBy);

        List<Service> services = new ArrayList<>();

        // Priority 1: Search by term (title or description)
        if (search != null && !search.trim().isEmpty()) {
            services = serviceRepository.searchByTitleOrDescription(search.trim());
        }
        // Priority 2: Filter by category ID
        else if (categoryId != null) {
            services = filterByCategory(categoryId, minPrice, maxPrice, sortBy);
        }
        // Priority 3: Filter by category name
        else if (categoryName != null && !categoryName.trim().isEmpty()) {
            Category category = categoryRepository.findByName(categoryName.trim())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryName));
            services = filterByCategory(category.getId(), minPrice, maxPrice, sortBy);
        }
        // Priority 4: Filter by price range only
        else if (minPrice != null || maxPrice != null) {
            BigDecimal min = minPrice != null ? minPrice : BigDecimal.ZERO;
            BigDecimal max = maxPrice != null ? maxPrice : new BigDecimal("999999.99");
            services = serviceRepository.findByPriceRange(min, max);
        }
        // Default: Get all active services
        else {
            services = serviceRepository.findByIsActiveTrueOrderByCreatedAtDesc();
        }

        // Apply sorting if needed (only if not already sorted by repository)
        if (sortBy != null && !sortBy.equals("newest") && (search != null || (minPrice == null && maxPrice == null))) {
            services = applySorting(services, sortBy);
        }

        return services.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get service by ID
     */
    @Transactional(readOnly = true)
    public ServiceResponse getServiceById(Long serviceId) {
        log.info("Fetching service with ID: {}", serviceId);

        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with ID: " + serviceId));

        if (!service.getIsActive()) {
            throw new ResourceNotFoundException("Service is not available");
        }

        return convertToResponse(service);
    }

    /**
     * Get services by category with sorting
     */
    @Transactional(readOnly = true)
    public List<ServiceResponse> getServicesByCategory(Integer categoryId, String sortBy) {
        log.info("Fetching services for category ID: {}, sortBy: {}", categoryId, sortBy);

        // Verify category exists
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + categoryId));

        List<Service> services;

        switch (sortBy.toLowerCase()) {
            case "price_low":
                services = serviceRepository.findByCategoryIdAndIsActiveTrueOrderByPriceAsc(categoryId);
                break;
            case "price_high":
                services = serviceRepository.findByCategoryIdAndIsActiveTrueOrderByPriceDesc(categoryId);
                break;
            case "newest":
            default:
                services = serviceRepository.findByCategoryIdAndIsActiveTrueOrderByCreatedAtDesc(categoryId);
                break;
        }

        return services.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Filter services by category with optional price range and sorting
     */
    private List<Service> filterByCategory(Integer categoryId, BigDecimal minPrice,
                                            BigDecimal maxPrice, String sortBy) {
        if (minPrice != null || maxPrice != null) {
            BigDecimal min = minPrice != null ? minPrice : BigDecimal.ZERO;
            BigDecimal max = maxPrice != null ? maxPrice : new BigDecimal("999999.99");
            return serviceRepository.findByCategoryAndPriceRange(categoryId, min, max);
        }

        if ("price_low".equalsIgnoreCase(sortBy)) {
            return serviceRepository.findByCategoryIdAndIsActiveTrueOrderByPriceAsc(categoryId);
        } else if ("price_high".equalsIgnoreCase(sortBy)) {
            return serviceRepository.findByCategoryIdAndIsActiveTrueOrderByPriceDesc(categoryId);
        }

        return serviceRepository.findByCategoryIdAndIsActiveTrueOrderByCreatedAtDesc(categoryId);
    }

    /**
     * Apply sorting to service list
     */
    private List<Service> applySorting(List<Service> services, String sortBy) {
        switch (sortBy.toLowerCase()) {
            case "price_low":
                return services.stream()
                        .sorted(Comparator.comparing(Service::getPrice))
                        .collect(Collectors.toList());
            case "price_high":
                return services.stream()
                        .sorted(Comparator.comparing(Service::getPrice).reversed())
                        .collect(Collectors.toList());
            case "newest":
            default:
                return services.stream()
                        .sorted(Comparator.comparing(Service::getCreatedAt).reversed())
                        .collect(Collectors.toList());
        }
    }

    /**
     * Convert Service entity to ServiceResponse DTO
     */
    private ServiceResponse convertToResponse(Service service) {
        return ServiceResponse.builder()
                .id(service.getId())
                .providerId(service.getProvider().getId())
                .providerName(service.getProvider().getFirstName() + " " + service.getProvider().getLastName())
                .categoryId(service.getCategory().getId())
                .categoryName(service.getCategory().getName())
                .title(service.getTitle())
                .description(service.getDescription())
                .price(service.getPrice())
                .durationMinutes(service.getDurationMinutes())
                .serviceType(service.getServiceType().name())
                .locationAddress(service.getLocationAddress())
                .isActive(service.getIsActive())
                .createdAt(service.getCreatedAt())
                .updatedAt(service.getUpdatedAt())
                .build();
    }
}

