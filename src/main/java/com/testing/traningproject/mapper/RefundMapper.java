package com.testing.traningproject.mapper;

import com.testing.traningproject.model.dto.response.RefundResponse;
import com.testing.traningproject.model.entity.Refund;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct Mapper for Refund entity
 */
@Mapper(componentModel = "spring")
public interface RefundMapper {

    @Mapping(source = "booking.id", target = "bookingId")
    @Mapping(source = "booking.service.title", target = "serviceTitle")
    @Mapping(target = "status", expression = "java(refund.getStatus().name())")
    RefundResponse toResponse(Refund refund);

    List<RefundResponse> toResponseList(List<Refund> refunds);
}

