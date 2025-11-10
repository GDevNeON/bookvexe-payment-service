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
    public Map<String, Object> testNotificationUnified(@RequestParam(required = false) UUID userId, @RequestParam(required = false) String toEmail, @RequestParam(required = false, defaultValue = "TEST_NOTIFICATION") String typeCode, @RequestParam(required = false, defaultValue = "false") Boolean sendEmail, @RequestParam(required = false) String title, @RequestParam(required = false) String message, @RequestParam(required = false) UUID bookingId, @RequestParam(required = false) UUID tripId, @RequestParam(required = false, defaultValue = "CHANNEL_TEST") String channel, @RequestParam(required = false, defaultValue = "false") Boolean shouldSave) {

//        UUID authenticatedUserId = getAuthenticatedUserId(authentication);
        UUID authenticatedUserId = userId;
        UUID targetUserId = userId != null ? userId : authenticatedUserId;

        String notificationTitle = title != null ? title : "Test Notification - " + typeCode;
        String notificationMessage = message != null ? message : "Notification Type: " + typeCode + (toEmail != null ? " | Email Override: " + toEmail : "") + " | Time: " + LocalDateTime.now();

        try {
            NotificationResponse response;
            String emailDetail = "N/A";
            String emailResolution = "not requested";

            if (Boolean.TRUE.equals(sendEmail)) {
                if (toEmail != null && !toEmail.isBlank()) {
                    response = notificationService.sendNotification(targetUserId, toEmail, typeCode, notificationTitle, notificationMessage, bookingId, tripId, channel, true, shouldSave);
                    emailDetail = "Sent to Override: " + toEmail;
                    emailResolution = "explicit_email";
                } else {
                    response = notificationService.sendNotification(targetUserId, typeCode, notificationTitle, notificationMessage, bookingId, tripId, channel, true, shouldSave);
                    emailDetail = "Resolved from available sources";
                    emailResolution = "auto_resolved";
                }
            } else {
                response = notificationService.sendNotification(targetUserId, typeCode, notificationTitle, notificationMessage, bookingId, tripId, channel, false, shouldSave);
                emailDetail = "Email disabled";
                emailResolution = "disabled";
            }

            return Map.of("status", "success", "message", "Notification sent successfully", "notification", response, "details", Map.of("targetUserId", targetUserId, "typeCode", typeCode, "sendEmailMode", emailDetail, "emailResolution", emailResolution, "savedToDB", shouldSave));

        } catch (Exception e) {
            log.error("Failed to send notification for user {}: {}", targetUserId, e.getMessage(), e);
            return Map.of("status", "error", "message", "Failed to send notification: " + e.getMessage());
        }
    }


    @PostMapping("/test-notification-by-booking")
    public Map<String, Object> testNotificationByBooking(@RequestParam(required = true) UUID bookingId, @RequestParam(required = false, defaultValue = "BOOKING_STATUS") String typeCode, @RequestParam(required = false, defaultValue = "true") Boolean sendEmail, @RequestParam(required = false) String title, @RequestParam(required = false) String message, @RequestParam(required = false, defaultValue = "CHANNEL_BOOKING") String channel, @RequestParam(required = false, defaultValue = "false") Boolean shouldSave // Control persistence
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