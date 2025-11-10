package com.example.bookvexe_payment_service.mappers;

import com.example.bookvexe_payment_service.models.db.NotificationTypeDbModel;
import com.example.bookvexe_payment_service.models.dto.notification.NotificationTypeResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationTypeMapper {

    NotificationTypeResponse toResponse(NotificationTypeDbModel entity);

}
