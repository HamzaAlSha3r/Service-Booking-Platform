package com.testing.traningproject.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Response DTO for TimeSlot
 * Contains available time slot information for customer booking
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlotResponse {
    private Long id;
    private Long serviceId;
    private String serviceTitle;
    private LocalDate slotDate;
    private String dayOfWeek; // MONDAY, TUESDAY, etc.
    private LocalTime startTime;
    private LocalTime endTime;
    private String status; // AVAILABLE, BOOKED, BLOCKED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

