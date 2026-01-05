package com.testing.traningproject.mapper;

import com.testing.traningproject.model.dto.response.SubscriptionPlanResponse;
import com.testing.traningproject.model.dto.response.SubscriptionResponse;
import com.testing.traningproject.model.entity.Subscription;
import com.testing.traningproject.model.entity.SubscriptionPlan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * MapStruct Mapper for Subscription entities
 */
@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    SubscriptionPlanResponse toPlanResponse(SubscriptionPlan plan);

    List<SubscriptionPlanResponse> toPlanResponseList(List<SubscriptionPlan> plans);

    @Mapping(source = "provider.id", target = "providerId")
    @Mapping(target = "providerName", expression = "java(subscription.getProvider().getFirstName() + \" \" + subscription.getProvider().getLastName())")
    @Mapping(source = "provider.email", target = "providerEmail")
    @Mapping(source = "plan.id", target = "planId")
    @Mapping(source = "plan.name", target = "planName")
    @Mapping(source = "plan.price", target = "planPrice")
    @Mapping(source = "plan.durationDays", target = "planDurationDays")
    @Mapping(target = "status", expression = "java(subscription.getStatus().name())")
    @Mapping(target = "daysRemaining", expression = "java(calculateDaysRemaining(subscription.getEndDate()))")
    SubscriptionResponse toResponse(Subscription subscription);

    default Long calculateDaysRemaining(LocalDate endDate) {
        long days = ChronoUnit.DAYS.between(LocalDate.now(), endDate);
        return days > 0 ? days : 0L;
    }
}

