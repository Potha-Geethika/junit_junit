package com.carbo.admin.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class Producer {
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void push(String topic, Object value) {
        try {
            kafkaTemplate.send(topic, value);
            log.info("Sent message to topic {} with value {}", topic, value);
        } catch (Exception e) {
            log.error("Error sending message to topic {}: {}", topic, e.getMessage());
            // You can also retry sending the message here if you want
        }
    }
}
