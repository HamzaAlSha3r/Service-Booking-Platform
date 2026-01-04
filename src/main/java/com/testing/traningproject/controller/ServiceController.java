package com.testing.traningproject.controller;

import com.testing.traningproject.model.dto.response.ServiceResponse;
import com.testing.traningproject.service.PublicServiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Public controller for Service browsing and search
 * Accessible to all users (no authentication required)
 */
@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
@Slf4j
public class ServiceController {

    private final PublicServiceService publicServiceService;

    /**
     * Get all active services
     * Supports filtering by category, search term, price range, and sorting
     */
    @GetMapping
    public ResponseEntity<List<ServiceResponse>> getAllServices(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false, defaultValue = "newest") String sortBy) {

        log.info("GET /api/services - categoryId: {}, categoryName: {}, search: {}, minPrice: {}, maxPrice: {}, sortBy: {}",
                categoryId, categoryName, search, minPrice, maxPrice, sortBy);

        List<ServiceResponse> services = publicServiceService.searchServices(
                categoryId, categoryName, search, minPrice, maxPrice, sortBy);

        return ResponseEntity.ok(services);
    }

    /**
     * Get service by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponse> getServiceById(@PathVariable Long id) {
        log.info("GET /api/services/{} - Fetching service by ID", id);
        ServiceResponse service = publicServiceService.getServiceById(id);
        return ResponseEntity.ok(service);
    }

    /**
     * Get services by category ID
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ServiceResponse>> getServicesByCategory(
            @PathVariable Integer categoryId,
            @RequestParam(required = false, defaultValue = "newest") String sortBy) {

        log.info("GET /api/services/category/{} - sortBy: {}", categoryId, sortBy);
        List<ServiceResponse> services = publicServiceService.getServicesByCategory(categoryId, sortBy);
        return ResponseEntity.ok(services);
    }
}

