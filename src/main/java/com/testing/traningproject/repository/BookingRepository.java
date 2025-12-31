package com.testing.traningproject.repository;

import com.testing.traningproject.model.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Booking entity
 * Provides database access methods for Booking table
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
}

