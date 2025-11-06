package com.example.bookvexe_payment_service.mappers;

import com.example.bookvexe_payment_service.models.db.PaymentDbModel;
import com.example.bookvexe_payment_service.models.dto.payment.PaymentResponse;
import com.example.bookvexe_payment_service.models.dto.payment.PaymentSelectResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {PaymentMethodMapper.class})
public interface PaymentMapper {

    @Mapping(target = "bookingId", source = "bookingId")
    PaymentResponse toResponse(PaymentDbModel entity);

    PaymentSelectResponse toSelectResponse(PaymentDbModel entity);

    @AfterMapping
    default void setDeleted(@MappingTarget PaymentResponse response, PaymentDbModel entity) {
        if (response != null && entity != null) {
            response.setIsDeleted(entity.getIsDeleted());
        }
    }
}
