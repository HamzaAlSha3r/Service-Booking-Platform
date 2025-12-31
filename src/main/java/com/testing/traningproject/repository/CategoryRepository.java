package com.testing.traningproject.repository;

import com.testing.traningproject.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Category entity
 * Provides database access methods for Category table
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}

