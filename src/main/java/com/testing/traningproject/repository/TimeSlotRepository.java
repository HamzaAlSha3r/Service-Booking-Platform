package com.testing.traningproject.repository;

import com.testing.traningproject.model.entity.TimeSlot;
import com.testing.traningproject.model.enums.TimeSlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Repository for TimeSlot entity
 * Provides database access methods for TimeSlot table
 */
@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    // Find available time slots for a service (ordered by date and time)
    List<TimeSlot> findByServiceIdAndStatusAndSlotDateGreaterThanEqualOrderBySlotDateAscStartTimeAsc(
            Long serviceId,
            TimeSlotStatus status,
            LocalDate fromDate
    );

    // Check if slot already exists
    boolean existsByServiceIdAndSlotDateAndStartTime(
            Long serviceId,
            LocalDate slotDate,
            LocalTime startTime
    );

    // Find available time slots for a service (legacy query)
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.service.id = :serviceId " +
           "AND ts.slotDate >= :fromDate " +
           "AND ts.status = 'AVAILABLE' " +
           "ORDER BY ts.slotDate, ts.startTime")
    List<TimeSlot> findAvailableSlotsByServiceId(
            @Param("serviceId") Long serviceId,
            @Param("fromDate") LocalDate fromDate
    );

    // Find time slot by ID and status
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.id = :slotId AND ts.status = :status")
    TimeSlot findByIdAndStatus(@Param("slotId") Long slotId, @Param("status") TimeSlotStatus status);
}

