package com.samic.handler;


import com.samic.events.ProductCreatedEvent;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics = "product-created-events-topic")
public class ProductEventsHandler {

    @KafkaHandler
    public void handle(ProductCreatedEvent event) {
        // Handle the product created event
        System.out.println("Received product created event: " + event);
    }
}
