package com.testing.traningproject.mapper;

import com.testing.traningproject.model.dto.response.BookingResponse;
import com.testing.traningproject.model.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct Mapper for Booking entity
 */
@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(target = "customerName", expression = "java(booking.getCustomer().getFirstName() + \" \" + booking.getCustomer().getLastName())")
    @Mapping(source = "service.id", target = "serviceId")
    @Mapping(source = "service.title", target = "serviceTitle")
    @Mapping(target = "providerName", expression = "java(booking.getService().getProvider().getFirstName() + \" \" + booking.getService().getProvider().getLastName())")
    @Mapping(source = "slot.slotDate", target = "slotDate")
    @Mapping(target = "dayOfWeek", expression = "java(booking.getSlot().getSlotDate().getDayOfWeek().name())")
    @Mapping(source = "slot.startTime", target = "startTime")
    @Mapping(source = "slot.endTime", target = "endTime")
    BookingResponse toResponse(Booking booking);

    List<BookingResponse> toResponseList(List<Booking> bookings);
}

