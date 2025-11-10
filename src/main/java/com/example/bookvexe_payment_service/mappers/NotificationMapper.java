package com.example.bookvexe_payment_service.mappers;

import com.example.bookvexe_payment_service.models.db.NotificationDbModel;
import com.example.bookvexe_payment_service.models.dto.notification.NotificationResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationResponse toResponse(NotificationDbModel entity);

}
