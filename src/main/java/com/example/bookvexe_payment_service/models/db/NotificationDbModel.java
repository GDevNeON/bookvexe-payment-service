package org.example.bookvexebej2e.models.db;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class NotificationDbModel extends BaseModel {
    @ManyToOne
    @JoinColumn(name = "userId")
    private UserDbModel user;

    @ManyToOne
    @JoinColumn(name = "bookingId", nullable = true)
    private BookingDbModel booking;

    @ManyToOne
    @JoinColumn(name = "tripId", nullable = true)
    private TripDbModel trip;

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