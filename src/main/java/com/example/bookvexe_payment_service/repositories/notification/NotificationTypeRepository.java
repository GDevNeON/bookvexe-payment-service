package org.example.bookvexebej2e.repositories.notification;


import org.example.bookvexebej2e.models.db.NotificationTypeDbModel;
import org.example.bookvexebej2e.repositories.base.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationTypeRepository extends BaseRepository<NotificationTypeDbModel> {
    Optional<NotificationTypeDbModel> findByCode(String typeCode);
}
