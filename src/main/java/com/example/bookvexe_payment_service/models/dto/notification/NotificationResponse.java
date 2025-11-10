package org.example.bookvexebej2e.models.dto.notification;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.bookvexebej2e.models.dto.base.BasePermissionResponse;
import org.example.bookvexebej2e.models.dto.booking.BookingResponse;
import org.example.bookvexebej2e.models.dto.trip.TripResponse;
import org.example.bookvexebej2e.models.dto.user.UserResponse;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
public class NotificationResponse extends BasePermissionResponse {
    private UUID id;
    private UserResponse user;
    private BookingResponse booking;
    private TripResponse trip;
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

    public NotificationResponse() {
        super();
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
        setPermissionsByDeletedStatus(isDeleted);
    }
}
