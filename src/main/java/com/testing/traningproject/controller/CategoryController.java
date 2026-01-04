package com.testing.traningproject.controller;

import com.testing.traningproject.model.dto.response.CategoryResponse;
import com.testing.traningproject.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public controller for Category operations
 * Accessible to all users (no authentication required)
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Get all active categories
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllActiveCategories() {
        List<CategoryResponse> categories = categoryService.getAllActiveCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * Get category by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Integer id) {
        log.info("GET /api/categories/{} - Fetching category by ID", id);
        CategoryResponse category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }
}

