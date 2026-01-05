package com.testing.traningproject.mapper;

import com.testing.traningproject.model.dto.response.PendingProviderResponse;
import com.testing.traningproject.model.dto.response.PendingRefundResponse;
import com.testing.traningproject.model.entity.Refund;
import com.testing.traningproject.model.entity.Role;
import com.testing.traningproject.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * MapStruct Mapper for Admin-specific responses
 */
@Mapper(componentModel = "spring")
public interface AdminMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(source = "professionalTitle", target = "professionalTitle")
    @Mapping(source = "bio", target = "bio")
    @Mapping(source = "certificateUrl", target = "certificateUrl")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))")
    PendingProviderResponse toPendingProviderResponse(User user);

    List<PendingProviderResponse> toPendingProviderResponseList(List<User> users);

    @Mapping(source = "booking.id", target = "bookingId")
    @Mapping(target = "customerName", expression = "java(refund.getBooking().getCustomer().getFirstName() + \" \" + refund.getBooking().getCustomer().getLastName())")
    @Mapping(source = "booking.customer.email", target = "customerEmail")
    @Mapping(source = "booking.service.title", target = "serviceName")
    @Mapping(target = "providerName", expression = "java(refund.getBooking().getService().getProvider().getFirstName() + \" \" + refund.getBooking().getService().getProvider().getLastName())")
    @Mapping(source = "booking.bookingDate", target = "bookingDate")
    PendingRefundResponse toPendingRefundResponse(Refund refund);

    List<PendingRefundResponse> toPendingRefundResponseList(List<Refund> refunds);

    /**
     * Custom mapping for roles (Set<Role> → Set<String>)
     */
    default Set<String> mapRoles(Set<Role> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
    }
}

