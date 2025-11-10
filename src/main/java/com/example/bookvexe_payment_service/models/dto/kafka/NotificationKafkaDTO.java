package com.example.bookvexe_payment_service.models.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationKafkaDTO {
    private UUID userId;
    private String username;
    private String email;
    private String phone;
    private String type;
}

