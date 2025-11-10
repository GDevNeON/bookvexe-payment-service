package com.example.bookvexe_payment_service.models.dto.notification;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class NotificationResponse {
    private UUID id;
    private UUID userId;
    private UUID bookingId;
    private UUID tripId;
    private NotificationTypeResponse type;
    private String channel;
    private String title;
    private String message;
    private Boolean isSent;
    private LocalDateTime sentAt;
    private Boolean isRead;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private Boolean isDeleted;
}
