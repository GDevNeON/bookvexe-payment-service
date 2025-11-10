package com.example.bookvexe_payment_service.services.payment.impl;

import com.example.bookvexe_payment_service.exceptions.ResourceNotFoundException;
import com.example.bookvexe_payment_service.mappers.NotificationMapper;
import com.example.bookvexe_payment_service.models.db.NotificationDbModel;
import com.example.bookvexe_payment_service.models.db.NotificationTypeDbModel;
import com.example.bookvexe_payment_service.models.dto.notification.NotificationResponse;
import com.example.bookvexe_payment_service.repositories.notification.NotificationRepository;
import com.example.bookvexe_payment_service.repositories.notification.NotificationTypeRepository;
import com.example.bookvexe_payment_service.services.payment.NotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    private final MailingService mailingService;
    private final WebSocketService webSocketService;
    private final NotificationMapper notificationMapper;
    private final NotificationTypeRepository notificationTypeRepository;
    private final NotificationRepository notificationRepository;

    @Transactional
    public NotificationResponse sendNotification(UUID userId, String typeCode, String title, String message, UUID bookingId, UUID tripId, String channel, Boolean sendEmail, Boolean shouldSave) {

        return sendNotificationInternal(userId, null, typeCode, title, message, bookingId, tripId, channel, sendEmail, shouldSave);
    }


    @Transactional
    public NotificationResponse sendNotification(UUID userId, String toEmail, String typeCode, String title, String message, UUID bookingId, UUID tripId, String channel, Boolean sendEmail, Boolean shouldSave) {

        return sendNotificationInternal(userId, toEmail, typeCode, title, message, bookingId, tripId, channel, sendEmail, shouldSave);
    }

    // -------------------------------------------------------------
    // Internal Method to Avoid Code Repetition
    // -------------------------------------------------------------

    private NotificationResponse sendNotificationInternal(UUID userId, String toEmail, String typeCode, String title, String message, UUID bookingId, UUID tripId, String channel, Boolean sendEmail, Boolean shouldSave) {

        NotificationDbModel entity;

        if (Boolean.TRUE.equals(shouldSave)) {
            // 1. Save Notification (Persistence)
            entity = saveNotification(userId, typeCode, title, message, bookingId, tripId, channel);
            log.info("Notification saved and sent to user {}. ID: {}", userId, entity.getId());
        } else {
            // 1b. Create unsaved entity for response and logging (No Persistence)
            entity = createUnsavedNotification(userId, typeCode, title, message, bookingId, tripId, channel);
            log.info("DEBUG Notification created (unsaved) and sent to user {}.", userId);
        }

        // 2. Send Email if requested
        if (Boolean.TRUE.equals(sendEmail)) {
            if (toEmail != null && !toEmail.isBlank()) {
                // Use explicit email if provided
                mailingService.sendEmail(toEmail, title, message);
            } else {
                // Fallback to user ID lookup
                mailingService.sendEmailToUser(userId, title, message);
            }
        }

        // 3. Ping Frontend via WebSocket
        webSocketService.notifyUser(userId, "NEW_NOTIFICATION");

        return notificationMapper.toResponse(entity);
    }

    // Helper for unsaved response
    private NotificationDbModel createUnsavedNotification(UUID userId, String typeCode, String title, String message, UUID bookingId, UUID tripId, String channel) {

        NotificationDbModel entity = new NotificationDbModel();
        entity.setId(UUID.randomUUID()); // Give it a temporary ID
        entity.setChannel(channel);
        entity.setTitle(title);
        entity.setMessage(message);
        entity.setIsRead(false);
        entity.setIsSent(true);
        entity.setSentAt(LocalDateTime.now());
        entity.setCreatedDate(LocalDateTime.now());
        entity.setUpdatedDate(LocalDateTime.now());
        entity.setCreatedBy(userId);
        entity.setUpdatedBy(userId);

        if (typeCode != null && !typeCode.trim().isEmpty()) {
            NotificationTypeDbModel type = notificationTypeRepository.findByCode(typeCode).orElseThrow(() -> new ResourceNotFoundException(NotificationTypeDbModel.class, typeCode));
            entity.setType(type);
        }

        entity.setBookingId(bookingId);
        entity.setTripId(tripId);

        return entity;
    }

    /**
     * Saves the notification entity to the database.
     */
    private NotificationDbModel saveNotification(UUID userId, String typeCode, String title, String message, UUID bookingId, UUID tripId, String channel) {
        NotificationDbModel entity = new NotificationDbModel();
        entity.setUserId(userId);
        entity.setChannel(channel != null ? channel : "APP");
        entity.setTitle(title);
        entity.setMessage(message);
        entity.setIsRead(false);
        entity.setIsSent(true);
        entity.setSentAt(LocalDateTime.now());

        if (typeCode != null && !typeCode.trim().isEmpty()) {
            NotificationTypeDbModel type = notificationTypeRepository.findByCode(typeCode).orElseThrow(() -> new ResourceNotFoundException(NotificationTypeDbModel.class, typeCode));
            entity.setType(type);
        }

        entity.setBookingId(bookingId);
        entity.setTripId(tripId);

        return notificationRepository.save(entity);
    }
}
