package org.example.bookvexebej2e.models.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MailKafkaDTO {
    private String to;
    private String subject;
    private String body;
    private String templateName;
    private Map<String, Object> templateModel;
}

