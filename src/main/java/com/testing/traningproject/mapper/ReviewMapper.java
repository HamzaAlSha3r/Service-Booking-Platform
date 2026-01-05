package com.testing.traningproject.mapper;

import com.testing.traningproject.model.dto.response.ReviewResponse;
import com.testing.traningproject.model.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct Mapper for Review entity
 */
@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(source = "booking.id", target = "bookingId")
    @Mapping(source = "booking.customer.firstName", target = "customerName")
    @Mapping(source = "booking.service.title", target = "serviceTitle")
    ReviewResponse toResponse(Review review);

    List<ReviewResponse> toResponseList(List<Review> reviews);
}

