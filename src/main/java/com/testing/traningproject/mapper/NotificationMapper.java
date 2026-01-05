package com.testing.traningproject.mapper;

import com.testing.traningproject.model.dto.response.NotificationResponse;
import com.testing.traningproject.model.entity.Notification;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct Mapper for Notification entity
 */
@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationResponse toResponse(Notification notification);

    List<NotificationResponse> toResponseList(List<Notification> notifications);
}

