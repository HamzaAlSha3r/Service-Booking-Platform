package com.testing.traningproject.service;

import com.testing.traningproject.exception.ResourceNotFoundException;
import com.testing.traningproject.mapper.TimeSlotMapper;
import com.testing.traningproject.model.dto.response.TimeSlotResponse;
import com.testing.traningproject.model.entity.ProviderAvailability;
import com.testing.traningproject.model.entity.TimeSlot;
import com.testing.traningproject.model.enums.DayOfWeek;
import com.testing.traningproject.model.enums.TimeSlotStatus;
import com.testing.traningproject.repository.ProviderAvailabilityRepository;
import com.testing.traningproject.repository.ServiceRepository;
import com.testing.traningproject.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Time Slot Service
 * Generates and manages time slots for services based on provider availability
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;
    private final ServiceRepository serviceRepository;
    private final ProviderAvailabilityRepository providerAvailabilityRepository;
    private final TimeSlotMapper timeSlotMapper; // ✅ MapStruct mapper

    /**
     * Get available time slots for a service (next 30 days)
     */
    @Transactional(readOnly = true)
    public List<TimeSlotResponse> getAvailableTimeSlotsForService(Long serviceId) {
        log.info("Fetching available time slots for service ID: {}", serviceId);

        // Validate service exists
        com.testing.traningproject.model.entity.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));

        // Generate time slots for the next 30 days if not already generated
        generateTimeSlotsForService(service);

        // Get available time slots (AVAILABLE status only)
        List<TimeSlot> availableSlots = timeSlotRepository
                .findByServiceIdAndStatusAndSlotDateGreaterThanEqualOrderBySlotDateAscStartTimeAsc(
                        serviceId,
                        TimeSlotStatus.AVAILABLE,
                        LocalDate.now()
                );

        return timeSlotMapper.toResponseList(availableSlots);
    }

    /**
     * Generate time slots for a service based on provider's availability
     * Generates slots for the next 30 days
     */
    @Transactional
    public void generateTimeSlotsForService(com.testing.traningproject.model.entity.Service service) {
        Long providerId = service.getProvider().getId();
        Integer serviceDuration = service.getDurationMinutes();

        // Get provider's availability
        List<ProviderAvailability> availabilities =
                providerAvailabilityRepository.findByProviderIdAndIsActiveTrue(providerId);

        if (availabilities.isEmpty()) {
            log.warn("No availability set for provider ID: {}", providerId);
            return;
        }

        // Generate slots for next 30 days
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(30);

        for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
            // Get day of week for this date
            java.time.DayOfWeek javaDayOfWeek = date.getDayOfWeek();
            DayOfWeek dayOfWeek = convertToDayOfWeek(javaDayOfWeek);

            // Find availability for this day
            List<ProviderAvailability> dayAvailability = availabilities.stream()
                    .filter(av -> av.getDayOfWeek() == dayOfWeek)
                    .toList();

            // Generate slots for each availability period
            for (ProviderAvailability availability : dayAvailability) {
                generateSlotsForPeriod(service, date, availability.getStartTime(),
                        availability.getEndTime(), serviceDuration);
            }
        }

        log.info("Time slots generated successfully for service ID: {}", service.getId());
    }

    /**
     * Generate time slots for a specific date and time period
     */
    private void generateSlotsForPeriod(com.testing.traningproject.model.entity.Service service,
                                        LocalDate date,
                                        LocalTime startTime,
                                        LocalTime endTime,
                                        Integer durationMinutes) {
        LocalTime currentTime = startTime;

        while (currentTime.plusMinutes(durationMinutes).isBefore(endTime)
                || currentTime.plusMinutes(durationMinutes).equals(endTime)) {

            LocalTime slotEndTime = currentTime.plusMinutes(durationMinutes);

            // Check if slot already exists
            boolean exists = timeSlotRepository.existsByServiceIdAndSlotDateAndStartTime(
                    service.getId(), date, currentTime);

            if (!exists) {
                // Create new time slot
                TimeSlot timeSlot = TimeSlot.builder()
                        .service(service)
                        .slotDate(date)
                        .startTime(currentTime)
                        .endTime(slotEndTime)
                        .status(TimeSlotStatus.AVAILABLE)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                timeSlotRepository.save(timeSlot);
            }

            currentTime = slotEndTime;
        }
    }

    /**
     * Convert java.time.DayOfWeek to custom DayOfWeek enum
     */
    private DayOfWeek convertToDayOfWeek(java.time.DayOfWeek javaDayOfWeek) {
        return switch (javaDayOfWeek) {
            case MONDAY -> DayOfWeek.MONDAY;
            case TUESDAY -> DayOfWeek.TUESDAY;
            case WEDNESDAY -> DayOfWeek.WEDNESDAY;
            case THURSDAY -> DayOfWeek.THURSDAY;
            case FRIDAY -> DayOfWeek.FRIDAY;
            case SATURDAY -> DayOfWeek.SATURDAY;
            case SUNDAY -> DayOfWeek.SUNDAY;
        };
    }
}

