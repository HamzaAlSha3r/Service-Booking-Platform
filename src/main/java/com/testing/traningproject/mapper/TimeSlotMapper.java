package com.testing.traningproject.mapper;

import com.testing.traningproject.model.dto.response.ProviderAvailabilityResponse;
import com.testing.traningproject.model.dto.response.TimeSlotResponse;
import com.testing.traningproject.model.entity.ProviderAvailability;
import com.testing.traningproject.model.entity.TimeSlot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct Mapper for TimeSlot and ProviderAvailability entities
 */
@Mapper(componentModel = "spring")
public interface TimeSlotMapper {

    @Mapping(source = "service.id", target = "serviceId")
    @Mapping(source = "service.title", target = "serviceTitle")
    @Mapping(target = "status", expression = "java(timeSlot.getStatus().name())")
    TimeSlotResponse toResponse(TimeSlot timeSlot);

    List<TimeSlotResponse> toResponseList(List<TimeSlot> timeSlots);

    @Mapping(source = "provider.id", target = "providerId")
    @Mapping(target = "providerName", expression = "java(availability.getProvider().getFirstName() + \" \" + availability.getProvider().getLastName())")
    @Mapping(target = "dayOfWeek", expression = "java(availability.getDayOfWeek().name())")
    ProviderAvailabilityResponse toAvailabilityResponse(ProviderAvailability availability);

    List<ProviderAvailabilityResponse> toAvailabilityResponseList(List<ProviderAvailability> availabilities);
}

