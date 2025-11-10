package com.example.bookvexe_payment_service.services.notification;

import com.example.bookvexe_payment_service.models.dto.notification.NotificationResponse;

import java.util.UUID;

public interface NotificationService {
    NotificationResponse sendNotification(UUID userId, String typeCode, String title, String message, UUID bookingId, UUID tripId, String channel, Boolean sendEmail, Boolean shouldSave);

    NotificationResponse sendNotification(UUID userId, String toEmail, String typeCode, String title, String message, UUID bookingId, UUID tripId, String channel, Boolean sendEmail, Boolean shouldSave);

    NotificationResponse sendNotificationByBookingId(String typeCode, String notificationTitle, String notificationMessage, UUID bookingId, String channel, Boolean sendEmail, Boolean shouldSave);
}
