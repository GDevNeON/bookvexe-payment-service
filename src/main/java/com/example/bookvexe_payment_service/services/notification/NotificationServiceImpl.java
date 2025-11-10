package com.example.bookvexe_payment_service.services.notification;

import com.example.bookvexe_payment_service.exceptions.ResourceNotFoundException;
import com.example.bookvexe_payment_service.mappers.NotificationMapper;
import com.example.bookvexe_payment_service.models.db.NotificationDbModel;
import com.example.bookvexe_payment_service.models.db.NotificationTypeDbModel;
import com.example.bookvexe_payment_service.models.dto.booking.BookingContextInfo;
import com.example.bookvexe_payment_service.models.dto.notification.NotificationResponse;
import com.example.bookvexe_payment_service.repositories.notification.NotificationRepository;
import com.example.bookvexe_payment_service.repositories.notification.NotificationTypeRepository;
import com.example.bookvexe_payment_service.services.core.CoreDataService;
import com.example.bookvexe_payment_service.services.external.MailingService;
import com.example.bookvexe_payment_service.services.external.WebSocketService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
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
    private final CoreDataService coreDataService;

    @Transactional
    public NotificationResponse sendNotification(UUID userId, String typeCode, String title, String message, UUID bookingId, UUID tripId, String channel, Boolean sendEmail, Boolean shouldSave) {
        return sendNotificationInternal(userId, null, typeCode, title, message, bookingId, tripId, channel, sendEmail, shouldSave, Optional.empty());
    }


    @Transactional
    public NotificationResponse sendNotification(UUID userId, String toEmail, String typeCode, String title, String message, UUID bookingId, UUID tripId, String channel, Boolean sendEmail, Boolean shouldSave) {
        return sendNotificationInternal(userId, toEmail, typeCode, title, message, bookingId, tripId, channel, sendEmail, shouldSave, Optional.empty());
    }

    // -------------------------------------------------------------
    // High-level Booking ID method
    // -------------------------------------------------------------

    /**
     * Sends a notification triggered by a booking, automatically retrieving
     * the target user, trip, and contact emails from the Core DB.
     */
    @Transactional
    public NotificationResponse sendNotificationByBookingId(String typeCode, String title, String message, UUID bookingId, String channel, Boolean sendEmail, Boolean shouldSave) {
        if (bookingId == null) {
            log.error("Cannot send notification: Booking ID is required.");
            throw new IllegalArgumentException("Booking ID cannot be null for sendNotificationByBookingId.");
        }

        Optional<BookingContextInfo> contextOpt = coreDataService.getBookingContextInfo(bookingId);

        if (contextOpt.isEmpty()) {
            log.warn("Booking {} not found or missing contact info. Notification aborted.", bookingId);
            throw new ResourceNotFoundException(BookingContextInfo.class, bookingId);
        }

        BookingContextInfo info = contextOpt.get();

        // Use the fetched IDs/info to call the internal process
        return sendNotificationInternal(info.userId(), // Target User ID for persistence and websocket
            null, // No explicit 'toEmail' override here
            typeCode, title, message, bookingId, info.tripId(), // Use the Trip ID from context info
            channel, sendEmail, shouldSave, contextOpt // Pass the context info for dynamic email lookup
        );
    }

    private NotificationResponse sendNotificationInternal(UUID userId, String toEmail, String typeCode, String title, String message, UUID bookingId, UUID tripId, String channel, Boolean sendEmail, Boolean shouldSave, Optional<BookingContextInfo> contextOpt // Context for dynamic email/ID lookup
    ) {

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

            String finalRecipientEmail = toEmail;

            if (finalRecipientEmail == null || finalRecipientEmail.isBlank()) {
                // If no explicit 'toEmail' override, look up the email via context info (if available)
                if (contextOpt.isPresent()) {
                    BookingContextInfo info = contextOpt.get();

                    // Priority check: 1. Customer Email, 2. Employee Email
                    finalRecipientEmail = info.customerEmail();
                    if (finalRecipientEmail == null || finalRecipientEmail.isBlank()) {
                        finalRecipientEmail = info.employeeEmail();
                    }

                    if (finalRecipientEmail == null || finalRecipientEmail.isBlank()) {
                        log.warn("Cannot find customer/employee email for Booking {} in context. Email skipped.", bookingId);
                    }
                } else {
                    // This branch is hit by the legacy `sendNotification(userId, ...)` calls.
                    // The original `mailingService.sendEmailToUser(userId, ...)` is no longer supported.
                    log.error("Email requested but neither 'toEmail' nor 'BookingContextInfo' was provided for user {}. Email skipped.", userId);
                }
            }

            if (finalRecipientEmail != null && !finalRecipientEmail.isBlank()) {
                mailingService.sendEmail(finalRecipientEmail, title, message);
            }
        }

        // 3. Ping Frontend via WebSocket (relies on the Kafka implementation in WebSocketService)
        // This relies on the target userId fetched from the context or provided directly.
        webSocketService.notifyUser(userId, "NEW_NOTIFICATION");

        return notificationMapper.toResponse(entity);
    }

    // Helper for unsaved response
    private NotificationDbModel createUnsavedNotification(UUID userId, String typeCode, String title, String message, UUID bookingId, UUID tripId, String channel) {

        NotificationDbModel entity = new NotificationDbModel();
        entity.setId(UUID.randomUUID());
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