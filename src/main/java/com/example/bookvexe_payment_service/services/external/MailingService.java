package com.example.bookvexe_payment_service.services.external;

import com.example.bookvexe_payment_service.models.dto.kafka.MailKafkaDTO;
import com.example.bookvexe_payment_service.services.kafka.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailingService {
    private final KafkaProducerService kafkaProducerService;

    public void sendEmail(String toEmail, String subject, String body) {
        sendMailRequestToKafka(toEmail, subject, body);
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

    public void sendEmailToUser(UUID userId, String subject, String body) {
        userRepository.findById(userId)
            .ifPresentOrElse(user -> {
                String email = getUserEmail(user);
                if (email != null) {
                    sendMailRequestToKafka(email, subject, body);
                } else {
                    log.warn("User {} has no associated email address (Customer/Employee) for sending.", userId);
                }
            }, () -> {
                log.warn("Attempted to send email to non-existent user ID: {}", userId);
            });
    }

    private String getUserEmail(UserDbModel user) {
        if (user.getCustomer() != null) {
            return user.getCustomer()
                .getEmail();
        }
        if (user.getEmployee() != null) {
            return user.getEmployee()
                .getEmail();
        }
        return null;
    }
}