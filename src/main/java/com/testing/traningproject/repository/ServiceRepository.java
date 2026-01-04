package com.testing.traningproject.repository;

import com.testing.traningproject.model.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository for Service entity
 * Provides database access methods for Service table
 */
@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {

    /**
     * Find all services by provider ID ordered by creation date descending
     */
    List<Service> findByProviderIdOrderByCreatedAtDesc(Long providerId);

    /**
     * Find active services by provider ID
     */
    List<Service> findByProviderIdAndIsActiveTrue(Long providerId);

    /**
     * Find all active services
     */
    List<Service> findByIsActiveTrueOrderByCreatedAtDesc();

    /**
     * Find services by category ID
     */
    List<Service> findByCategoryIdAndIsActiveTrueOrderByCreatedAtDesc(Integer categoryId);

    /**
     * Search services by title (case-insensitive)
     */
    @Query("SELECT s FROM Service s WHERE LOWER(s.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND s.isActive = true ORDER BY s.createdAt DESC")
    List<Service> searchByTitle(@Param("searchTerm") String searchTerm);

    /**
     * Search services by title OR description
     */
    @Query("SELECT s FROM Service s WHERE (LOWER(s.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(s.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND s.isActive = true ORDER BY s.createdAt DESC")
    List<Service> searchByTitleOrDescription(@Param("searchTerm") String searchTerm);

    /**
     * Find services by category and price range
     */
    @Query("SELECT s FROM Service s WHERE s.category.id = :categoryId AND s.price BETWEEN :minPrice AND :maxPrice AND s.isActive = true ORDER BY s.price ASC")
    List<Service> findByCategoryAndPriceRange(@Param("categoryId") Integer categoryId, @Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    /**
     * Find services by price range only
     */
    @Query("SELECT s FROM Service s WHERE s.price BETWEEN :minPrice AND :maxPrice AND s.isActive = true ORDER BY s.price ASC")
    List<Service> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    /**
     * Find services by category ordered by price (low to high)
     */
    List<Service> findByCategoryIdAndIsActiveTrueOrderByPriceAsc(Integer categoryId);

    /**
     * Find services by category ordered by price (high to low)
     */
    List<Service> findByCategoryIdAndIsActiveTrueOrderByPriceDesc(Integer categoryId);

    /**
     * Count active services by category
     */
    long countByCategoryIdAndIsActiveTrue(Integer categoryId);

    /**
     * Count all active services
     */
    long countByIsActiveTrue();
}

