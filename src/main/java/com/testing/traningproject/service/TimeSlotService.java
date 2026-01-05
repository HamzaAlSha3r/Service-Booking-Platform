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
     * Get available time slots for a service with optional date filtering
     * @param serviceId Service ID
     * @param fromDateStr Start date (YYYY-MM-DD) or null for today
     * @param toDateStr End date (YYYY-MM-DD) or null for +7 days from start
     * @param limit Maximum number of slots to return (default: 50)
     */
    @Transactional(readOnly = true)
    public List<TimeSlotResponse> getAvailableTimeSlotsForService(
            Long serviceId, String fromDateStr, String toDateStr, int limit) {

        log.info("Fetching available time slots for service ID: {} (from: {}, to: {}, limit: {})",
                serviceId, fromDateStr, toDateStr, limit);

        // Validate service exists
        com.testing.traningproject.model.entity.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));

        // Generate time slots for the next 30 days if not already generated
        generateTimeSlotsForService(service);

        // Parse dates
        LocalDate fromDate = (fromDateStr != null && !fromDateStr.isBlank())
                ? LocalDate.parse(fromDateStr)
                : LocalDate.now();

        LocalDate toDate = (toDateStr != null && !toDateStr.isBlank())
                ? LocalDate.parse(toDateStr)
                : fromDate.plusDays(30); // Default: next 30 days (matches slot generation period)

        // Get available time slots within date range
        List<TimeSlot> availableSlots = timeSlotRepository
                .findByServiceIdAndStatusAndSlotDateBetweenOrderBySlotDateAscStartTimeAsc(
                        serviceId,
                        TimeSlotStatus.AVAILABLE,
                        fromDate,
                        toDate
                );

        // Apply limit
        if (limit > 0 && availableSlots.size() > limit) {
            availableSlots = availableSlots.subList(0, limit);
        }

        log.info("Found {} available slots for service ID: {}", availableSlots.size(), serviceId);

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
     * Creates slots based on service duration (e.g., 120-minute service = one 2-hour slot)
     */
    private void generateSlotsForPeriod(com.testing.traningproject.model.entity.Service service,
                                        LocalDate date,
                                        LocalTime startTime,
                                        LocalTime endTime,
                                        Integer durationMinutes) {
        LocalTime currentTime = startTime;

        // Generate slots until we can't fit a full service duration
        while (currentTime.plusMinutes(durationMinutes).isBefore(endTime)
                || currentTime.plusMinutes(durationMinutes).equals(endTime)) {

            LocalTime slotEndTime = currentTime.plusMinutes(durationMinutes);

            // Check if slot already exists
            boolean exists = timeSlotRepository.existsByServiceIdAndSlotDateAndStartTime(
                    service.getId(), date, currentTime);

            if (!exists) {
                // Create new time slot with service duration
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

                log.debug("Created slot: {} - {} to {}", date, currentTime, slotEndTime);
            }

            // Move to next slot (don't overlap - jump by full service duration)
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

    /**
     * Get all time slots for a provider's services with optional filters
     */
    @Transactional(readOnly = true)
    public List<TimeSlotResponse> getProviderTimeSlots(
            Long providerId, Long serviceId, String fromDateStr, String toDateStr, String statusStr) {

        log.info("Fetching time slots for provider ID: {} (serviceId: {}, from: {}, to: {}, status: {})",
                providerId, serviceId, fromDateStr, toDateStr, statusStr);

        // Parse dates
        LocalDate fromDate = (fromDateStr != null && !fromDateStr.isBlank())
                ? LocalDate.parse(fromDateStr)
                : LocalDate.now();

        LocalDate toDate = (toDateStr != null && !toDateStr.isBlank())
                ? LocalDate.parse(toDateStr)
                : fromDate.plusDays(30);

        // Parse status
        TimeSlotStatus status = (statusStr != null && !statusStr.isBlank())
                ? TimeSlotStatus.valueOf(statusStr.toUpperCase())
                : null;

        List<TimeSlot> slots;

        if (serviceId != null) {
            // Filter by specific service
            if (status != null) {
                slots = timeSlotRepository.findByServiceIdAndStatusAndSlotDateBetweenOrderBySlotDateAscStartTimeAsc(
                        serviceId, status, fromDate, toDate);
            } else {
                slots = timeSlotRepository.findByServiceIdAndSlotDateBetweenOrderBySlotDateAscStartTimeAsc(
                        serviceId, fromDate, toDate);
            }
        } else {
            // Get all slots for provider's services
            List<com.testing.traningproject.model.entity.Service> services =
                    serviceRepository.findByProviderIdOrderByCreatedAtDesc(providerId);

            List<Long> serviceIds = services.stream()
                    .map(com.testing.traningproject.model.entity.Service::getId)
                    .toList();

            if (status != null) {
                slots = timeSlotRepository.findByServiceIdInAndStatusAndSlotDateBetweenOrderBySlotDateAscStartTimeAsc(
                        serviceIds, status, fromDate, toDate);
            } else {
                slots = timeSlotRepository.findByServiceIdInAndSlotDateBetweenOrderBySlotDateAscStartTimeAsc(
                        serviceIds, fromDate, toDate);
            }
        }

        return timeSlotMapper.toResponseList(slots);
    }

    /**
     * Block a time slot (prevent bookings)
     */
    @Transactional
    public TimeSlotResponse blockTimeSlot(Long providerId, Long slotId) {
        log.info("Provider ID: {} blocking time slot ID: {}", providerId, slotId);

        TimeSlot slot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Time slot not found"));

        // Verify ownership
        if (!slot.getService().getProvider().getId().equals(providerId)) {
            throw new com.testing.traningproject.exception.ForbiddenException(
                    "You don't have permission to block this time slot");
        }

        // Check if already booked
        if (slot.getStatus() == TimeSlotStatus.BOOKED) {
            throw new com.testing.traningproject.exception.BadRequestException(
                    "Cannot block a time slot that is already booked");
        }

        slot.setStatus(TimeSlotStatus.BLOCKED);
        slot.setUpdatedAt(LocalDateTime.now());

        timeSlotRepository.save(slot);

        log.info("Time slot ID: {} blocked successfully", slotId);
        return timeSlotMapper.toResponse(slot);
    }

    /**
     * Unblock a time slot (make it available again)
     */
    @Transactional
    public TimeSlotResponse unblockTimeSlot(Long providerId, Long slotId) {
        log.info("Provider ID: {} unblocking time slot ID: {}", providerId, slotId);

        TimeSlot slot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Time slot not found"));

        // Verify ownership
        if (!slot.getService().getProvider().getId().equals(providerId)) {
            throw new com.testing.traningproject.exception.ForbiddenException(
                    "You don't have permission to unblock this time slot");
        }

        // Check if currently blocked
        if (slot.getStatus() != TimeSlotStatus.BLOCKED) {
            throw new com.testing.traningproject.exception.BadRequestException(
                    "Time slot is not blocked. Current status: " + slot.getStatus());
        }

        slot.setStatus(TimeSlotStatus.AVAILABLE);
        slot.setUpdatedAt(LocalDateTime.now());

        timeSlotRepository.save(slot);

        log.info("Time slot ID: {} unblocked successfully", slotId);
        return timeSlotMapper.toResponse(slot);
    }
}
