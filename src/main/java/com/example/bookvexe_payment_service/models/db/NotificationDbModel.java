package com.example.bookvexe_payment_service.models.db;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class NotificationDbModel extends BaseModel {
    @Column(name = "userId")
    private UUID userId;

    @Column(name = "bookingId", nullable = true)
    private UUID bookingId;

    @Column(name = "tripId", nullable = true)
    private UUID tripId;

    @ManyToOne
    @JoinColumn(name = "typeId", nullable = true)
    private NotificationTypeDbModel type;

    @Column(length = 20, name = "channel")
    private String channel;

    @Column(length = 100, name = "title")
    private String title;

    @Column(columnDefinition = "TEXT", name = "message")
    private String message;

    @Column(name = "isSent")
    private Boolean isSent;

    @Column(name = "sentAt")
    private LocalDateTime sentAt;

    @Column(name = "isRead")
    private Boolean isRead = false;
}