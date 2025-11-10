package org.example.bookvexebej2e.services.notification;

import org.example.bookvexebej2e.models.dto.notification.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.domain.Page;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;
import java.util.UUID;
public interface NotificationService {
    List<NotificationResponse> findAll();

    Page<NotificationResponse> findAll(NotificationQuery query);

    NotificationResponse findById(UUID id);

    NotificationResponse create(NotificationCreate createDto);

    NotificationResponse update(UUID id, NotificationUpdate updateDto);

    void delete(UUID id);

    void activate(UUID id);

    void deactivate(UUID id);

    List<NotificationSelectResponse> findAllForSelect();

    Page<NotificationSelectResponse> findAllForSelect(NotificationQuery query);

    // New high-level service method for use by other modules
    NotificationResponse sendNotification(UUID userId, String typeCode, String title, String message, UUID bookingId,
        UUID tripId, String channel, Boolean sendEmail, Boolean shouldSave);

    NotificationResponse sendNotification(UUID userId, String toEmail, String typeCode, String title, String message,
        UUID bookingId, UUID tripId, String channel, Boolean sendEmail, Boolean shouldSave);


    // user-facing methods
    Page<NotificationResponse> getMyNotifications(UUID userId, NotificationQuery query);

    void markNotificationAsRead(UUID notificationId, UUID userId);

    void deleteNotification(UUID notificationId, UUID userId);

    int countUnreadNotifications(UUID userId);
}
