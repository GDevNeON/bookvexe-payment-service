package com.example.bookvexe_payment_service.mappers;

import com.example.bookvexe_payment_service.models.db.PaymentMethodDbModel;
import com.example.bookvexe_payment_service.models.dto.payment.PaymentMethodResponse;
import com.example.bookvexe_payment_service.models.dto.payment.PaymentMethodSelectResponse;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PaymentMethodMapper {

    PaymentMethodResponse toResponse(PaymentMethodDbModel entity);

    PaymentMethodSelectResponse toSelectResponse(PaymentMethodDbModel entity);

    @AfterMapping
    default void setDeleted(@MappingTarget PaymentMethodResponse response, PaymentMethodDbModel entity) {
        if (response != null && entity != null) {
            response.setIsDeleted(entity.getIsDeleted());
        }
    }
}
