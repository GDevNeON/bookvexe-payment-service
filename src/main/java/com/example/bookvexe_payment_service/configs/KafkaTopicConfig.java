package com.example.bookvexe_payment_service.configs;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String NOTIFICATION_TOPIC = "notification-topic";
    public static final String MAIL_TOPIC = "mail-topic";

    @Bean
    public NewTopic notificationTopic() {
        return TopicBuilder.name(NOTIFICATION_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic mailTopic() {
        return TopicBuilder.name(MAIL_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }
}