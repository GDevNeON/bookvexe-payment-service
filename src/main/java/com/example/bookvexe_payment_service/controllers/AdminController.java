package com.example.bookvexe_payment_service.controllers;


import com.example.bookvexe_payment_service.models.dto.notification.NotificationResponse;
import com.example.bookvexe_payment_service.services.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final NotificationService notificationService;

    @GetMapping("/hello")
    public Map<String, String> hello() {
        return Map.of("message", "Hello World");
    }

    @PostMapping("/test-notification")
    public Map<String, Object> testNotificationUnified(@RequestParam(required = true) UUID userId, // Target user ID
                                                       @RequestParam(required = false) String toEmail, // Email override (optional)
                                                       @RequestParam(required = false, defaultValue = "TEST_NOTIFICATION") String typeCode, @RequestParam(required = false, defaultValue = "false") Boolean sendEmail, @RequestParam(required = false) String title, @RequestParam(required = false) String message, @RequestParam(required = false) UUID bookingId, @RequestParam(required = false) UUID tripId, @RequestParam(required = false, defaultValue = "CHANNEL_TEST") String channel, @RequestParam(required = false, defaultValue = "false") Boolean shouldSave // Control persistence
    ) {

        // 2. Prepare content
        String notificationTitle = title != null ? title : "Test Notification - " + typeCode;
        String notificationMessage = message != null ? message : "Notification Type: " + typeCode + (toEmail != null ? " | Email Override: " + toEmail : "") + " | Time: " + LocalDateTime.now();

        // 3. Determine which method to call
        try {
            NotificationResponse response;
            String emailDetail = "N/A";

            if (Boolean.TRUE.equals(sendEmail) && toEmail != null && !toEmail.isBlank()) {
                response = notificationService.sendNotification(userId, toEmail, // Use the explicit email
                    typeCode, notificationTitle, notificationMessage, bookingId, tripId, channel, true, shouldSave);
                emailDetail = "Sent to Override: " + toEmail;

            } else {
                response = notificationService.sendNotification(userId, typeCode, notificationTitle, notificationMessage, bookingId, tripId, channel, sendEmail, // Use the parameter value
                    shouldSave);
                emailDetail = Boolean.TRUE.equals(sendEmail) ? "Sent via User Lookup" : "Email disabled";
            }

            return Map.of("status", "success", "message", "Notification sent successfully", "notification", response, "details", Map.of("targetUserId", userId, "typeCode", typeCode, "sendEmailMode", emailDetail, "savedToDB", shouldSave));

        } catch (Exception e) {
            log.error("Failed to send notification for user {}: {}", userId, e.getMessage(), e);
            return Map.of("status", "error", "message", "Failed to send notification: " + e.getMessage());
        }
    }


    @PostMapping("/test-notification-by-booking")
    public Map<String, Object> testNotificationByBooking(@RequestParam(required = true) UUID bookingId, // REQUIRED: Booking ID to fetch context
                                                         @RequestParam(required = false, defaultValue = "BOOKING_STATUS") String typeCode, @RequestParam(required = false, defaultValue = "true") Boolean sendEmail, @RequestParam(required = false) String title, @RequestParam(required = false) String message, @RequestParam(required = false, defaultValue = "CHANNEL_BOOKING") String channel, @RequestParam(required = false, defaultValue = "false") Boolean shouldSave // Control persistence
    ) {
        // 2. Prepare content
        String notificationTitle = title != null ? title : "Booking Status Update - " + typeCode;
        String notificationMessage = message != null ? message : "Notification for Booking ID: " + bookingId + " | Type: " + typeCode + " | Time: " + LocalDateTime.now();

        // 3. Call the new high-level service method
        try {
            NotificationResponse response = notificationService.sendNotificationByBookingId(typeCode, notificationTitle, notificationMessage, bookingId, channel, sendEmail, shouldSave);

            String emailDetail = Boolean.TRUE.equals(sendEmail) ? "Attempted via Customer/Employee Lookup" : "Email disabled";


            return Map.of("status", "success", "message", "Notification sent successfully (via Booking ID lookup)", "notification", response, "details", Map.of("bookingId", bookingId, "typeCode", typeCode, "sendEmailMode", emailDetail, "savedToDB", shouldSave));

        } catch (Exception e) {
            log.error("Failed to send notification for booking {}: {}", bookingId, e.getMessage(), e);
            return Map.of("status", "error", "message", "Failed to send notification: " + e.getMessage());
        }
    }

}