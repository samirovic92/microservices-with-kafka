package com.samic.service;

import com.samic.events.ProductCreatedEvent;
import com.samic.rest.dto.CreateProductRequest;
import com.sun.net.httpserver.Headers;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@AllArgsConstructor
@Service
public class ProductService {
    private final KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;


    public String createProduct(CreateProductRequest product) throws Exception {
        var productId = UUID.randomUUID().toString();
        var productCreatedEvent = ProductCreatedEvent.builder()
                .productId(productId)
                .title(product.title())
                .price(product.price())
                .quantity(product.quantity())
                .build();

        log.info("send productCreatedEvent  : {}", productCreatedEvent);

        ProducerRecord<String, ProductCreatedEvent> record = new ProducerRecord<>(
                "product-created-events-topic",
                productId,
                productCreatedEvent
        );
        record.headers().add("messageId", productId.getBytes());

        SendResult<String, ProductCreatedEvent> result = kafkaTemplate.send(record).get();
        log.info("Topic : {}", result.getRecordMetadata().topic());
        log.info("Partition : {}", result.getRecordMetadata().partition());
        log.info("offset : {}", result.getRecordMetadata().offset());

        // future.whenComplete((result, exception) -> {
        //     if (Objects.nonNull(exception)) {
        //         log.error("***** Error sending message: {} *****", exception.getMessage());
        //     } else {
        //         log.info("***** Message sent successfully: {} *****", result.getRecordMetadata());
        //     }
        // });
        log.info("***** create product successfully *****");
        return productId;
    }
}
