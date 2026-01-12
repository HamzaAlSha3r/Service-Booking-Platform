package com.testing.traningproject.repository;

import com.testing.traningproject.model.entity.Booking;
import com.testing.traningproject.model.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Booking entity
 * Provides database access methods for Booking table
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Find all bookings for a customer
    @Query("SELECT b FROM Booking b WHERE b.customer.id = :customerId ORDER BY b.createdAt DESC")
    List<Booking> findByCustomerId(@Param("customerId") Long customerId);

    // Find all bookings grouped by service
    @Query("SELECT b FROM Booking b INNER JOIN FETCH b.service INNER JOIN FETCH b.customer ORDER BY b.service.id")
    List<Booking> findAllBookingForService();

    // Find all bookings for a provider
    @Query("SELECT b FROM Booking b WHERE b.service.provider.id = :providerId ORDER BY b.createdAt DESC")
    List<Booking> findByProviderId(@Param("providerId") Long providerId);

    // Find booking by ID and customer ID (security check)
    @Query("SELECT b FROM Booking b WHERE b.id = :bookingId AND b.customer.id = :customerId")
    Optional<Booking> findByIdAndCustomerId(@Param("bookingId") Long bookingId, @Param("customerId") Long customerId);

    // Find booking by ID and provider ID (security check)
    @Query("SELECT b FROM Booking b WHERE b.id = :bookingId AND b.service.provider.id = :providerId")
    Optional<Booking> findByIdAndProviderId(@Param("bookingId") Long bookingId, @Param("providerId") Long providerId);

    // Count bookings by status
    long countByStatus(BookingStatus status);

    // Find all bookings by status
    List<Booking> findByStatus(BookingStatus status);
}

