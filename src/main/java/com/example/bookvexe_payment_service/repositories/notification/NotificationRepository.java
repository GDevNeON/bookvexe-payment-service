package com.example.bookvexe_payment_service.repositories.notification;


import com.example.bookvexe_payment_service.models.db.NotificationDbModel;
import com.example.bookvexe_payment_service.repositories.base.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends BaseRepository<NotificationDbModel> {
}
