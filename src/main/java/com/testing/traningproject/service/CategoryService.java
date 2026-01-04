package com.testing.traningproject.service;

import com.testing.traningproject.exception.DuplicateResourceException;
import com.testing.traningproject.exception.ResourceNotFoundException;
import com.testing.traningproject.model.dto.request.CategoryRequest;
import com.testing.traningproject.model.dto.response.CategoryResponse;
import com.testing.traningproject.model.entity.Category;
import com.testing.traningproject.repository.CategoryRepository;
import com.testing.traningproject.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for Category management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ServiceRepository serviceRepository;

    /**
     * Get all categories (admin only - includes inactive)
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        log.info("Fetching all categories (admin view)");

        List<Category> categories = categoryRepository.findAllByOrderByNameAsc();

        return categories.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all active categories
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllActiveCategories() {
        log.info("Fetching all active categories");

        List<Category> categories = categoryRepository.findByIsActiveTrue();

        return categories.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }


    /**
     * Get category by ID
     */
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Integer categoryId) {
        log.info("Fetching category with ID: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + categoryId));

        return convertToResponse(category);
    }

    /**
     * Create new category (admin only)
     */
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        // Check if category with same name already exists (case-insensitive)
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Category already exists with name: " + request.getName());
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .iconUrl(request.getIconUrl())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Category savedCategory = categoryRepository.save(category);
        log.info("Category created successfully with ID: {}", savedCategory.getId());

        return convertToResponse(savedCategory);
    }

    /**
     * Update existing category (admin only)
     */
    @Transactional
    public CategoryResponse updateCategory(Integer categoryId, CategoryRequest request) {
        log.info("Updating category with ID: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + categoryId));

        // Check if new name conflicts with existing category (case-insensitive)
        if (request.getName() != null && !request.getName().equalsIgnoreCase(category.getName())) {
            if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
                throw new DuplicateResourceException("Category already exists with name: " + request.getName());
            }
            category.setName(request.getName());
        }

        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }

        if (request.getIconUrl() != null) {
            category.setIconUrl(request.getIconUrl());
        }

        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }

        category.setUpdatedAt(LocalDateTime.now());

        Category updatedCategory = categoryRepository.save(category);
        log.info("Category updated successfully: {}", categoryId);

        return convertToResponse(updatedCategory);
    }

    /**
     * Delete category (admin only)
     * Only if no services are using it
     */
    @Transactional
    public void deleteCategory(Integer categoryId) {
        log.info("Deleting category with ID: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + categoryId));

        // Check if category has services
        long serviceCount = serviceRepository.countByCategoryIdAndIsActiveTrue(categoryId);
        if (serviceCount > 0) {
            throw new IllegalStateException("Cannot delete category. " + serviceCount + " active services are using this category.");
        }

        categoryRepository.delete(category);
        log.info("Category deleted successfully: {}", categoryId);
    }

    /**
     * Convert Category entity to CategoryResponse DTO
     */
    private CategoryResponse convertToResponse(Category category) {
        long totalServices = serviceRepository.countByCategoryIdAndIsActiveTrue(category.getId());


        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .iconUrl(category.getIconUrl())
                .isActive(category.getIsActive())
                .totalServices(totalServices)
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}

