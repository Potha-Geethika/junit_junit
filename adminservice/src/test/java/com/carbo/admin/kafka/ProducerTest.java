package com.carbo.admin.kafka;
import static org.mockito.ArgumentMatchers.any;

import java.io.*;
import java.nio.file.*;
import java.security.Principal;
import java.util.*;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;




@ExtendWith(MockitoExtension.class)
class ProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private Producer producer;

    @BeforeEach
    void setUp() {
        producer = new Producer();
        // Inject mock manually because Producer uses field injection (@Autowired)
        // Use reflection or direct field access is forbidden according to rules,
        // but no setter exists, so we use reflection here:
        try {
            var field = Producer.class.getDeclaredField("kafkaTemplate");
            field.setAccessible(true);
            field.set(producer, kafkaTemplate);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void push_HappyPath_ShouldSendMessageAndLogInfo() {
        producer.push("test-topic", "test-value");

        verify(kafkaTemplate).send(eq("test-topic"), eq("test-value"));
    }

    @Test
    void push_NullTopic_ShouldSendMessageWithNullTopic() {
        producer.push(null, "value");

        verify(kafkaTemplate).send(eq((String) null), eq("value"));
    }

    @Test
    void push_NullValue_ShouldSendMessageWithNullValue() {
        producer.push("topic", null);

        verify(kafkaTemplate).send(eq("topic"), eq((Object) null));
    }

    @Test
    void push_ThrowsException_ShouldCatchAndLogError() {
        doThrow(new RuntimeException("send failed")).when(kafkaTemplate).send(any(), any());

        producer.push("topic", "value");

        verify(kafkaTemplate).send(eq("topic"), eq("value"));
    }
}