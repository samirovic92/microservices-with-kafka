package com.samic.ProductService.service;

import com.samic.ProductService.rest.dto.CreateProductRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@AllArgsConstructor
@Service
public class ProductService {
    private final KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;


    public String createProduct(CreateProductRequest product) {
        var productId = UUID.randomUUID().toString();
        var productCreatedEvent = ProductCreatedEvent.builder()
                .productId(productId)
                .title(product.title())
                .price(product.price())
                .quantity(product.quantity())
                .build();
        CompletableFuture<SendResult<String, ProductCreatedEvent>> future =
                kafkaTemplate.send("product-created-events-topic", productId, productCreatedEvent);

        future.whenComplete((result, exception) -> {
            if (Objects.nonNull(exception)) {
                log.error("***** Error sending message: {} *****", exception.getMessage());
            } else {
                log.info("***** Message sent successfully: {} *****", result.getRecordMetadata());
            }
        });
        log.info("***** create product successfully *****");
        return productId;
    }
}
