package com.example.bookvexe_payment_service.services.external;

import com.example.bookvexe_payment_service.models.dto.kafka.MailKafkaDTO;
import com.example.bookvexe_payment_service.services.core.CoreDataService;
import com.example.bookvexe_payment_service.services.kafka.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailingService {
    private final KafkaProducerService kafkaProducerService;
    private final CoreDataService coreDataService; // New Dependency

    /**
     * Public method to send an email to an explicit address.
     */
    public void sendEmail(String toEmail, String subject, String body) {
        sendMailRequestToKafka(toEmail, subject, body);
    }

    /**
     * High-level function to send an email to the customer associated with a Booking.
     * It relies on CoreDataService to look up the email address.
     */
    public void sendEmailByBookingId(UUID bookingId, String subject, String body) {
        coreDataService.getBookingContextInfo(bookingId)
            .ifPresentOrElse(info -> {

                // 1. Prioritize Customer Email
                String email = info.customerEmail();
                String source = "customer";

                // 2. Fallback to Employee Email if Customer Email is missing/blank
                if (email == null || email.isBlank()) {
                    email = info.employeeEmail();
                    source = "employee";
                }

                if (email != null && !email.isBlank()) {
                    sendMailRequestToKafka(email, subject, body);
                    log.info("Email request sent for booking {} using {} email: {}", bookingId, source, email);
                } else {
                    // Both customer and employee emails are missing/blank
                    log.warn("Booking {} has no associated customer or employee email for sending.", bookingId);
                }
            }, () -> {
                log.warn("Attempted to send email for non-existent booking ID: {}", bookingId);
            });
    }


    private void sendMailRequestToKafka(String toEmail, String subject, String body) {
        if (toEmail == null || toEmail.isBlank()) {
            log.warn("Attempted to send mail with empty recipient. Subject: {}", subject);
            return;
        }

        kafkaProducerService.sendMail(new MailKafkaDTO(toEmail, subject, body, null, // templateName
            new HashMap<>() // templateModel
        ));
        log.info("Successfully sent email request to mail service through Kafka. To: {}, Subject: {}", toEmail,
            subject);
    }
}