package com.samic.handler;

import com.samic.entity.ProcessedEventEntity;
import com.samic.events.ProductCreatedEvent;
import com.samic.repository.ProcessedEventRepository;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.messaging.Message;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@EmbeddedKafka
@SpringBootTest(properties = "kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
class ProductEventsHandlerIntegrationTest {
    @MockitoBean
    ProcessedEventRepository processedEventRepository;
    @MockitoBean
    RestTemplate restTemplate;
    @Autowired
    KafkaTemplate kafkaTemplate;
    @MockitoSpyBean
    ProductEventsHandler productEventsHandler;

    @Test
    void handle_product_created_event() throws ExecutionException, InterruptedException {
        // Given
        var productCreatedEvent = new ProductCreatedEvent(
                UUID.randomUUID().toString(),
                "Test Product",
                BigDecimal.valueOf(19.99),
                100);
        var messageId = productCreatedEvent.getProductId();
        var messageKey = productCreatedEvent.getProductId();
        ProducerRecord<String, ProductCreatedEvent> record = new ProducerRecord<>(
                "product-created-events-topic",
                messageKey,
                productCreatedEvent);
        record.headers().add("messageId", messageId.getBytes());
        record.headers().add(KafkaHeaders.RECEIVED_KEY, messageKey.getBytes());

        when(restTemplate.getForEntity("http://localhost:8090/mock/200", String.class))
                .thenReturn(ResponseEntity.ok("Success"));
        when(processedEventRepository.save(any(ProcessedEventEntity.class))).thenReturn(null);

        // When
        kafkaTemplate.send(record).get();

        // Then
        ArgumentCaptor<String> messageIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> messageKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ProductCreatedEvent> productCreatedEventCaptor = ArgumentCaptor.forClass(ProductCreatedEvent.class);
        verify(productEventsHandler, timeout(3000).times(1))
                .handle(productCreatedEventCaptor.capture(), messageIdCaptor.capture(), messageKeyCaptor.capture());
        assertEquals(messageId, messageIdCaptor.getValue());
        assertEquals(messageKey, messageKeyCaptor.getValue());
        assertEquals(productCreatedEvent, productCreatedEventCaptor.getValue());
    }


}