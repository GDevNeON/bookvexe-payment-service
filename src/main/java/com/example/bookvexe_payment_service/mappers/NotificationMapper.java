package org.example.bookvexebej2e.mappers;

import org.example.bookvexebej2e.models.db.NotificationDbModel;
import org.example.bookvexebej2e.models.dto.notification.NotificationResponse;
import org.example.bookvexebej2e.models.dto.notification.NotificationSelectResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.AfterMapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationResponse toResponse(NotificationDbModel entity);

    NotificationSelectResponse toSelectResponse(NotificationDbModel entity);

    @AfterMapping
    default void setPermissions(@MappingTarget NotificationResponse response, NotificationDbModel entity) {
        if (response != null && entity != null) {
            response.setIsDeleted(entity.getIsDeleted());
        }
    }
}
