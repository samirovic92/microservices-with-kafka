package com.samic.handler;


import com.samic.error.NotRetryableException;
import com.samic.error.RetryableException;
import com.samic.events.ProductCreatedEvent;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@AllArgsConstructor
@Component
@KafkaListener(topics = "product-created-events-topic")
public class ProductEventsHandler {

    private final static String request_url = "http://localhost:8090/mock";
    private final RestTemplate restTemplate;

    @KafkaHandler
    public void handle(ProductCreatedEvent event) {
        System.out.println("Received product created event: " + event);
        try {
            var success_request_url = request_url + "/200";
            var failure_request_url = request_url + "/500";
            var response = restTemplate.getForEntity(failure_request_url, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Received response: " + response.getBody());
            }
        } catch (ResourceAccessException e) {
            throw new RetryableException(e);
        } catch (Exception e) {
            throw new NotRetryableException(e);
        }
    }
}
