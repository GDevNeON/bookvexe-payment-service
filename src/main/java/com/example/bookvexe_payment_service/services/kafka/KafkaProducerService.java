package org.example.bookvexebej2e.services.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bookvexebej2e.configs.KafkaTopicConfig;
import org.example.bookvexebej2e.models.dto.kafka.MailKafkaDTO;
import org.example.bookvexebej2e.models.dto.kafka.NotificationKafkaDTO;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendNotification(NotificationKafkaDTO notificationDto) {
        try {
            log.info("Sending notification to Kafka: {}", notificationDto);
            kafkaTemplate.send(KafkaTopicConfig.NOTIFICATION_TOPIC, notificationDto);
        } catch (Exception e) {
            log.error("Error sending notification to Kafka", e);
        }
    }

    public void sendMail(MailKafkaDTO mailDto) {
        try {
            log.info("Sending mail request to Kafka: {}", mailDto);
            kafkaTemplate.send(KafkaTopicConfig.MAIL_TOPIC, mailDto);
        } catch (Exception e) {
            log.error("Error sending mail request to Kafka", e);
        }
    }
}

