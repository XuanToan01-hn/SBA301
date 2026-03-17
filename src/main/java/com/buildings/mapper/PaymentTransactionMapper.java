package com.buildings.mapper;

import com.buildings.dto.response.payment.PaymentTransactionDTO;
import com.buildings.entity.PaymentTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentTransactionMapper {

    @Mapping(source = "bill.id", target = "billId")
    @Mapping(source = "bill.periodCode", target = "billPeriodCode")
    @Mapping(source = "bill.apartment.code", target = "apartmentCode")
    @Mapping(target = "method", ignore = true)
    PaymentTransactionDTO toDto(PaymentTransaction entity);

    @Mapping(target = "bill", ignore = true)
    @Mapping(target = "postedBy", ignore = true)
    @Mapping(target = "proofUrl", ignore = true)
    @Mapping(target = "referenceNo", ignore = true)
    @Mapping(target = "paidAt", ignore = true)
    @Mapping(target = "rejectedReason", ignore = true)
    @Mapping(target = "verifiedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    PaymentTransaction toEntity(PaymentTransactionDTO dto);
}
