package com.testing.traningproject.mapper;

import com.testing.traningproject.model.dto.response.ServiceResponse;
import com.testing.traningproject.model.dto.response.ServiceWithBookingsResponse;
import com.testing.traningproject.model.entity.Service;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct Mapper for Service entity
 */
@Mapper(componentModel = "spring")
public interface ServiceMapper {

    @Mapping(source = "provider.id", target = "providerId")
    @Mapping(target = "providerName", expression = "java(service.getProvider().getFirstName() + \" \" + service.getProvider().getLastName())")
    @Mapping(source = "provider.email", target = "providerEmail")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(target = "serviceType", expression = "java(service.getServiceType().name())")
    ServiceResponse toResponse(Service service);

    List<ServiceResponse> toResponseList(List<Service> services);

    /**
     * Map Service to ServiceWithBookingsResponse (without bookings - will be set manually)
     */
    @Mapping(source = "provider.id", target = "providerId")
    @Mapping(target = "providerName", expression = "java(service.getProvider().getFirstName() + \" \" + service.getProvider().getLastName())")
    @Mapping(source = "provider.email", target = "providerEmail")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(target = "serviceType", expression = "java(service.getServiceType().name())")
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "totalBookings", ignore = true)
    @Mapping(target = "completedBookings", ignore = true)
    @Mapping(target = "cancelledBookings", ignore = true)
    @Mapping(target = "totalRevenue", ignore = true)
    ServiceWithBookingsResponse toServiceWithBookingsResponse(Service service);
}

