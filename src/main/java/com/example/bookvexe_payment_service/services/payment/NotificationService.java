package com.example.bookvexe_payment_service.services.payment;

import com.example.bookvexe_payment_service.models.dto.notification.NotificationResponse;

import java.util.UUID;

public interface NotificationService {
    NotificationResponse sendNotification(UUID userId, String typeCode, String title, String message, UUID bookingId, UUID tripId, String channel, Boolean sendEmail, Boolean shouldSave);

    NotificationResponse sendNotification(UUID userId, String toEmail, String typeCode, String title, String message, UUID bookingId, UUID tripId, String channel, Boolean sendEmail, Boolean shouldSave);
}
