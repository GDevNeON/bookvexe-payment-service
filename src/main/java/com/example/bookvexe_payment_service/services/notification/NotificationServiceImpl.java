package com.example.bookvexe_payment_service.services.notification;

import com.example.bookvexe_payment_service.exceptions.ResourceNotFoundException;
import com.example.bookvexe_payment_service.mappers.NotificationMapper;
import com.example.bookvexe_payment_service.models.db.NotificationDbModel;
import com.example.bookvexe_payment_service.models.db.NotificationTypeDbModel;
import com.example.bookvexe_payment_service.models.dto.context.BookingContextInfo;
import com.example.bookvexe_payment_service.models.dto.context.UserContextInfo;
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
        // For payment service, when no explicit email is provided but sendEmail is true,
        // we need to fetch the email from CoreDataService if bookingId is available
        Optional<BookingContextInfo> contextOpt = Optional.empty();

        if (Boolean.TRUE.equals(sendEmail) && bookingId != null) {
            contextOpt = coreDataService.getBookingContextInfo(bookingId);
            if (contextOpt.isEmpty()) {
                log.warn("Booking {} not found when trying to fetch email for notification", bookingId);
            }
        }

        return sendNotificationInternal(userId, null, typeCode, title, message, bookingId, tripId, channel, sendEmail, shouldSave, contextOpt);
    }

    @Transactional
    public NotificationResponse sendNotification(UUID userId, String toEmail, String typeCode, String title, String message, UUID bookingId, UUID tripId, String channel, Boolean sendEmail, Boolean shouldSave) {
        // If explicit email is provided, we don't need to fetch context for email
        Optional<BookingContextInfo> contextOpt = Optional.empty();
        return sendNotificationInternal(userId, toEmail, typeCode, title, message, bookingId, tripId, channel, sendEmail, shouldSave, contextOpt);
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

    private NotificationResponse sendNotificationInternal(UUID userId, String toEmail, String typeCode, String title, String message, UUID bookingId, UUID tripId, String channel, Boolean sendEmail, Boolean shouldSave, Optional<BookingContextInfo> contextOpt) {

        NotificationDbModel entity;

        if (Boolean.TRUE.equals(shouldSave)) {
            entity = saveNotification(userId, typeCode, title, message, bookingId, tripId, channel);
            log.info("Notification saved and sent to user {}. ID: {}", userId, entity.getId());
        } else {
            entity = createUnsavedNotification(userId, typeCode, title, message, bookingId, tripId, channel);
            log.info("DEBUG Notification created (unsaved) and sent to user {}.", userId);
        }

        // Enhanced email handling for payment service
        if (Boolean.TRUE.equals(sendEmail)) {
            String finalRecipientEmail = toEmail;

            // If no explicit email provided, try to resolve it
            if (finalRecipientEmail == null || finalRecipientEmail.isBlank()) {
                finalRecipientEmail = resolveRecipientEmail(userId, bookingId, contextOpt);
            }

            if (finalRecipientEmail != null && !finalRecipientEmail.isBlank()) {
                mailingService.sendEmail(finalRecipientEmail, title, message);
                log.info("Email sent to: {}", finalRecipientEmail);
            } else {
                log.warn("Email requested but no recipient email could be resolved for user {} and booking {}", userId, bookingId);
            }
        }

        webSocketService.notifyUser(userId, "NEW_NOTIFICATION");
        return notificationMapper.toResponse(entity);
    }

    /**
     * Resolves recipient email by trying multiple sources in priority order
     */
    private String resolveRecipientEmail(UUID userId, UUID bookingId, Optional<BookingContextInfo> contextOpt) {
        // If we already have context info from a previous call, use it
        if (contextOpt.isPresent()) {
            String email = contextOpt.get().getBestEmail();
            if (email != null && !email.isBlank()) {
                return email;
            }
        }

        // If no context but we have userId or bookingId, try to fetch user context
        if (userId != null || bookingId != null) {
            Optional<UserContextInfo> userContext = coreDataService.getUserContextInfo(userId, bookingId);
            if (userContext.isPresent()) {
                String email = userContext.get().getBestEmail();
                if (email != null && !email.isBlank()) {
                    return email;
                }
            }
        }

        log.warn("Could not resolve email for user {} with booking {}", userId, bookingId);
        return null;
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