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

    // Find available time slots within date range
    List<TimeSlot> findByServiceIdAndStatusAndSlotDateBetweenOrderBySlotDateAscStartTimeAsc(
            Long serviceId,
            TimeSlotStatus status,
            LocalDate fromDate,
            LocalDate toDate
    );

    // Find time slots within date range (any status)
    List<TimeSlot> findByServiceIdAndSlotDateBetweenOrderBySlotDateAscStartTimeAsc(
            Long serviceId,
            LocalDate fromDate,
            LocalDate toDate
    );

    // Find time slots for multiple services with status filter
    List<TimeSlot> findByServiceIdInAndStatusAndSlotDateBetweenOrderBySlotDateAscStartTimeAsc(
            List<Long> serviceIds,
            TimeSlotStatus status,
            LocalDate fromDate,
            LocalDate toDate
    );

    // Find time slots for multiple services (any status)
    List<TimeSlot> findByServiceIdInAndSlotDateBetweenOrderBySlotDateAscStartTimeAsc(
            List<Long> serviceIds,
            LocalDate fromDate,
            LocalDate toDate
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

    // Find time slots by service ID and day of week (for deletion when availability is removed)
    // Uses PostgreSQL day of week extraction - Sunday=0, Monday=1, ..., Saturday=6
    @Query(value = "SELECT * FROM time_slot ts " +
           "WHERE ts.service_id = :serviceId " +
           "AND EXTRACT(DOW FROM ts.slot_date) = " +
           "CASE :dayOfWeek " +
           "  WHEN 'MONDAY' THEN 1 " +
           "  WHEN 'TUESDAY' THEN 2 " +
           "  WHEN 'WEDNESDAY' THEN 3 " +
           "  WHEN 'THURSDAY' THEN 4 " +
           "  WHEN 'FRIDAY' THEN 5 " +
           "  WHEN 'SATURDAY' THEN 6 " +
           "  WHEN 'SUNDAY' THEN 0 " +
           "END",
           nativeQuery = true)
    List<TimeSlot> findByServiceIdAndDayOfWeek(
            @Param("serviceId") Long serviceId,
            @Param("dayOfWeek") String dayOfWeek
    );
}

