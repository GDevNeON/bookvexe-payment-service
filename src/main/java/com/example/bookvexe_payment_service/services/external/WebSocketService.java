package com.example.bookvexe_payment_service.services.external;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.example.bookvexebej2e.exceptions.ResourceNotFoundException;
import org.example.bookvexebej2e.models.db.UserDbModel;
import org.example.bookvexebej2e.models.dto.kafka.NotificationKafkaDTO;
import org.example.bookvexebej2e.repositories.user.UserRepository;
import org.example.bookvexebej2e.services.kafka.KafkaProducerService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final KafkaProducerService kafkaProducerService;
    private final SimpUserRegistry userRegistry;

    /**
     * Sends a real-time event to a specific user's private queue.
     * The frontend will subscribe to /user/{userId}/queue/notifications
     */
    public void notifyUserWithOwnWebsocket(UUID userId, String eventType) {
        UserDbModel user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException(UserDbModel.class, userId));

        String principalName = user.getUsername();
        log.info("Attempting to notify user: username={}, userId={}", principalName, userId);

        // DEBUG: Check what users are registered
        log.info("Total users in registry: {}", userRegistry.getUserCount());
        for (SimpUser simpUser : userRegistry.getUsers()) {
            log.info("Registered user: name={}, sessions={}", simpUser.getName(), simpUser.getSessions()
                .size());
        }

        // Check if our specific user is registered
        SimpUser simpUser = userRegistry.getUser(principalName);
        if (simpUser == null) {
            log.error("User '{}' NOT FOUND in registry!", principalName);
        } else {
            log.info("User '{}' found with {} sessions", principalName, simpUser.getSessions()
                .size());
        }

        try {
            messagingTemplate.convertAndSendToUser(principalName, "/queue/notifications", eventType);
            log.info("Successfully sent notification to user: {}", principalName);
        } catch (Exception e) {
            log.error("Failed to send notification to user: {}", principalName, e);
        }
    }

    private void notifyUserWithKafkaProducer(UUID userId, String eventType) {
        kafkaProducerService.sendNotification(new NotificationKafkaDTO(userId, null, null, null, eventType));
    }

    public void notifyUser(UUID userId, String eventType) {
        if (Strings.CI.contains(eventType, "NOTIFICATION")) {
            notifyUserWithKafkaProducer(userId, eventType);
            return;
        }
        notifyUserWithOwnWebsocket(userId, eventType);
    }
}