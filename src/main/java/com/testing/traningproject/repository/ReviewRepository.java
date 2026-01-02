package com.testing.traningproject.repository;

import com.testing.traningproject.model.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Review entity
 * Provides database access methods for Review table
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Check if review exists for a booking
    boolean existsByBookingId(Long bookingId);

    // Find review by booking ID
    Optional<Review> findByBookingId(Long bookingId);

    // Find all reviews for a provider
    @Query("SELECT r FROM Review r WHERE r.booking.service.provider.id = :providerId ORDER BY r.createdAt DESC")
    List<Review> findByProviderId(@Param("providerId") Long providerId);

    // Find all reviews for a service
    @Query("SELECT r FROM Review r WHERE r.booking.service.id = :serviceId ORDER BY r.createdAt DESC")
    List<Review> findByServiceId(@Param("serviceId") Long serviceId);

    // Calculate average rating for a provider
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.booking.service.provider.id = :providerId")
    Double calculateAverageRatingByProviderId(@Param("providerId") Long providerId);

    // Count reviews for a provider
    @Query("SELECT COUNT(r) FROM Review r WHERE r.booking.service.provider.id = :providerId")
    Long countByProviderId(@Param("providerId") Long providerId);
}

