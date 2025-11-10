package com.example.bookvexe_payment_service.services.external;

import com.example.bookvexe_payment_service.models.dto.kafka.NotificationKafkaDTO;
import com.example.bookvexe_payment_service.services.core.CoreDataService;
import com.example.bookvexe_payment_service.services.kafka.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

// Removed: SimpMessagingTemplate, UserRepository, SimpUserRegistry
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {

    private final KafkaProducerService kafkaProducerService;
    private final CoreDataService coreDataService;

    /**
     * Low-level function to notify a user via Kafka.
     * The Core Service is responsible for consuming this message and delivering it
     * to the user's active WebSocket session.
     */
    public void notifyUser(UUID userId, String eventType) {
        if (userId == null) {
            log.warn("Attempted to send notification with null userId. EventType: {}", eventType);
            return;
        }

        // This microservice always uses Kafka to communicate back to the Core Service
        kafkaProducerService.sendNotification(new NotificationKafkaDTO(userId, null, null, null, eventType));
        log.info("Successfully sent notification request to Core Service via Kafka. UserId: {}, EventType: {}", userId, eventType);
    }

    /**
     * High-level function to trigger a notification for the user who made a specific Booking.
     * It uses CoreDataService to look up the userId based on the bookingId.
     */
    public void notifyUserByBookingId(UUID bookingId, String eventType) {
        coreDataService.getBookingContextInfo(bookingId).ifPresentOrElse(info -> {
            UUID userId = info.userId();
            if (userId != null) {
                // Send notification to the user ID found in the Core DB
                notifyUser(userId, eventType);
            } else {
                log.warn("Booking {} has no associated UserDbModel (userId) for notification.", bookingId);
            }
        }, () -> {
            log.warn("Attempted to send notification for non-existent booking ID: {}", bookingId);
        });
    }
}