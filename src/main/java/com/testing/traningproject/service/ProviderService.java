package com.testing.traningproject.service;

import com.testing.traningproject.exception.BadRequestException;
import com.testing.traningproject.exception.ForbiddenException;
import com.testing.traningproject.exception.ResourceNotFoundException;
import com.testing.traningproject.model.dto.request.CreateServiceRequest;
import com.testing.traningproject.model.dto.request.SetAvailabilityRequest;
import com.testing.traningproject.model.dto.request.UpdateServiceRequest;
import com.testing.traningproject.model.dto.response.ProviderAvailabilityResponse;
import com.testing.traningproject.model.dto.response.ServiceResponse;
import com.testing.traningproject.model.entity.*;
import com.testing.traningproject.model.enums.DayOfWeek;
import com.testing.traningproject.model.enums.ServiceType;
import com.testing.traningproject.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * management Provider Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProviderService {

    private final ServiceRepository serviceRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ProviderAvailabilityRepository providerAvailabilityRepository;
    private final SubscriptionService subscriptionService;

    /**
     * Create a new service
     */
    @Transactional
    public ServiceResponse createService(Long providerId, CreateServiceRequest request) {
        // Validate provider
        User provider = userRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));

        // Check if provider has active subscription
        if (!subscriptionService.hasActiveSubscription(providerId)) {
            throw new ForbiddenException("You must have an active subscription to create services");
        }

        // Validate category
        Category category = categoryRepository.findById(Long.valueOf(request.getCategoryId()))
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!category.getIsActive()) {
            throw new BadRequestException("This category is not available");
        }

        // Validate location for IN_PERSON services
        ServiceType serviceType = ServiceType.valueOf(request.getServiceType());
        if ((serviceType == ServiceType.IN_PERSON || serviceType == ServiceType.BOTH)
            && (request.getLocationAddress() == null || request.getLocationAddress().isBlank())) {
            throw new BadRequestException("Location address is required for IN_PERSON or BOTH service types");
        }

        // 5. Create service
        com.testing.traningproject.model.entity.Service service = com.testing.traningproject.model.entity.Service.builder()
                .provider(provider)
                .category(category)
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .durationMinutes(request.getDurationMinutes())
                .serviceType(serviceType)
                .locationAddress(request.getLocationAddress())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        service = serviceRepository.save(service);

        log.info("Service created successfully - ID: {}", service.getId());

        return convertToServiceResponse(service);
    }

    /**
     * Update existing service
     */
    @Transactional
    public ServiceResponse updateService(Long providerId, Long serviceId, UpdateServiceRequest request) {
        // Get service and validate ownership
        com.testing.traningproject.model.entity.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));

        if (!service.getProvider().getId().equals(providerId)) {
            throw new ForbiddenException("You can only update your own services");
        }

        // Update fields if provided
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(Long.valueOf(request.getCategoryId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            service.setCategory(category);
        }

        if (request.getTitle() != null) {
            service.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            service.setDescription(request.getDescription());
        }

        if (request.getPrice() != null) {
            service.setPrice(request.getPrice());
        }

        if (request.getDurationMinutes() != null) {
            service.setDurationMinutes(request.getDurationMinutes());
        }

        if (request.getServiceType() != null) {
            ServiceType serviceType = ServiceType.valueOf(request.getServiceType());
            service.setServiceType(serviceType);
        }

        if (request.getLocationAddress() != null) {
            service.setLocationAddress(request.getLocationAddress());
        }

        if (request.getIsActive() != null) {
            service.setIsActive(request.getIsActive());
        }

        service.setUpdatedAt(LocalDateTime.now());
        service = serviceRepository.save(service);

        log.info("Service updated successfully - ID: {}", serviceId);

        return convertToServiceResponse(service);
    }

    /**
     * Delete service
     */
    @Transactional
    public void deleteService(Long providerId, Long serviceId) {
        com.testing.traningproject.model.entity.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));

        if (!service.getProvider().getId().equals(providerId)) {
            throw new ForbiddenException("You can only delete your own services");
        }

        serviceRepository.delete(service);

        log.info("Service deleted successfully - ID: {}", serviceId);
    }

    /**
     * Get provider's services
     */
    @Transactional(readOnly = true)
    public List<ServiceResponse> getProviderServices(Long providerId) {
        log.info("Fetching services for provider ID: {}", providerId);

        return serviceRepository.findByProviderIdOrderByCreatedAtDesc(providerId)
                .stream()
                .map(this::convertToServiceResponse)
                .collect(Collectors.toList());
    }

    // ==================== Availability Management ====================

    /**
     * Set provider availability
     */
    @Transactional
    public ProviderAvailabilityResponse setAvailability(Long providerId, SetAvailabilityRequest request) {
        // Validate provider
        User provider = userRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));

        // Validate times , must end time after start time
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new BadRequestException("End time must be after start time");
        }

        // Check for overlapping availability , if he creates availability time in same time previous
        DayOfWeek dayOfWeek = DayOfWeek.valueOf(request.getDayOfWeek());
        List<ProviderAvailability> existingAvailability =
            providerAvailabilityRepository.findByProviderIdAndDayOfWeek(providerId, dayOfWeek);

        for (ProviderAvailability existing : existingAvailability) {
            if (timeSlotsOverlap(request.getStartTime(), request.getEndTime(),
                                existing.getStartTime(), existing.getEndTime())) {
                throw new BadRequestException("This time slot overlaps with existing availability");
            }
        }

        // Create availability
        ProviderAvailability availability = ProviderAvailability.builder()
                .provider(provider)
                .dayOfWeek(dayOfWeek)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        availability = providerAvailabilityRepository.save(availability);

        log.info("Availability set successfully - ID: {}", availability.getId());

        return convertToAvailabilityResponse(availability);
    }

    /**
     * Get provider's availability
     */
    @Transactional(readOnly = true)
    public List<ProviderAvailabilityResponse> getProviderAvailability(Long providerId) {
        log.info("Fetching availability for provider ID: {}", providerId);

        return providerAvailabilityRepository.findByProviderIdOrderByDayOfWeekAsc(providerId)
                .stream()
                .map(this::convertToAvailabilityResponse)
                .collect(Collectors.toList());
    }

    /**
     * Delete availability
     */
    @Transactional
    public void deleteAvailability(Long providerId, Long availabilityId) {
        ProviderAvailability availability = providerAvailabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Availability not found"));

        if (!availability.getProvider().getId().equals(providerId)) {
            throw new ForbiddenException("You can only delete your own availability");
        }

        providerAvailabilityRepository.delete(availability);

        log.info("Availability deleted successfully - ID: {}", availabilityId);
    }

    // ==================== Helper Methods ====================

    private boolean timeSlotsOverlap(java.time.LocalTime start1, java.time.LocalTime end1,
                                     java.time.LocalTime start2, java.time.LocalTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    // ==================== methods to convert for Response Shape ====================

    private ServiceResponse convertToServiceResponse(com.testing.traningproject.model.entity.Service service) {
        return ServiceResponse.builder()
                .id(service.getId())
                .providerId(service.getProvider().getId())
                .providerName(service.getProvider().getFirstName() + " " + service.getProvider().getLastName())
                .providerEmail(service.getProvider().getEmail())
                .categoryId(service.getCategory().getId())
                .categoryName(service.getCategory().getName())
                .title(service.getTitle())
                .description(service.getDescription())
                .price(service.getPrice())
                .durationMinutes(service.getDurationMinutes())
                .serviceType(service.getServiceType().name())
                .locationAddress(service.getLocationAddress())
                .isActive(service.getIsActive())
                .createdAt(service.getCreatedAt())
                .updatedAt(service.getUpdatedAt())
                .build();
    }

    private ProviderAvailabilityResponse convertToAvailabilityResponse(ProviderAvailability availability) {
        return ProviderAvailabilityResponse.builder()
                .id(availability.getId())
                .providerId(availability.getProvider().getId())
                .providerName(availability.getProvider().getFirstName() + " " + availability.getProvider().getLastName())
                .dayOfWeek(availability.getDayOfWeek().name())
                .startTime(availability.getStartTime())
                .endTime(availability.getEndTime())
                .isActive(availability.getIsActive())
                .createdAt(availability.getCreatedAt())
                .updatedAt(availability.getUpdatedAt())
                .build();
    }
}

