package com.testing.traningproject.mapper;

import com.testing.traningproject.model.dto.response.TransactionResponse;
import com.testing.traningproject.model.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct Mapper for Transaction entity
 */
@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(target = "userName", expression = "java(transaction.getUser().getFirstName() + \" \" + transaction.getUser().getLastName())")
    @Mapping(source = "booking.id", target = "bookingId")
    @Mapping(source = "subscription.id", target = "subscriptionId")
    TransactionResponse toResponse(Transaction transaction);

    List<TransactionResponse> toResponseList(List<Transaction> transactions);
}

