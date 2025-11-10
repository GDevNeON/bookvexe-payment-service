package org.example.bookvexebej2e.mappers;

import org.example.bookvexebej2e.models.db.NotificationTypeDbModel;
import org.example.bookvexebej2e.models.dto.notification.NotificationTypeResponse;
import org.example.bookvexebej2e.models.dto.notification.NotificationTypeSelectResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.AfterMapping;

@Mapper(componentModel = "spring")
public interface NotificationTypeMapper {

    NotificationTypeResponse toResponse(NotificationTypeDbModel entity);

    NotificationTypeSelectResponse toSelectResponse(NotificationTypeDbModel entity);

    @AfterMapping
    default void setPermissions(@MappingTarget NotificationTypeResponse response, NotificationTypeDbModel entity) {
        if (response != null && entity != null) {
            response.setIsDeleted(entity.getIsDeleted());
        }
    }
}
