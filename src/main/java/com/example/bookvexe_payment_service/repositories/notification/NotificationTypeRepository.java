package com.example.bookvexe_payment_service.repositories.notification;


import com.example.bookvexe_payment_service.models.db.NotificationTypeDbModel;
import com.example.bookvexe_payment_service.repositories.base.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationTypeRepository extends BaseRepository<NotificationTypeDbModel> {
    Optional<NotificationTypeDbModel> findByCode(String typeCode);
}
