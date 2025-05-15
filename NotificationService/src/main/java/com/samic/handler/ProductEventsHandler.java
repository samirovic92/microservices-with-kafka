package com.samic.handler;


import com.samic.entity.ProcessedEventEntity;
import com.samic.error.NotRetryableException;
import com.samic.error.RetryableException;
import com.samic.events.ProductCreatedEvent;
import com.samic.repository.ProcessedEventRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@AllArgsConstructor
@Component
@KafkaListener(topics = "product-created-events-topic")
public class ProductEventsHandler {

    private final static String request_url = "http://localhost:8090/mock";
    private final RestTemplate restTemplate;
    private final ProcessedEventRepository processedEventRepository;

    @KafkaHandler
    public void handle(@Payload ProductCreatedEvent event,
                       @Header("messageId") String messageId,
                       @Header(KafkaHeaders.RECEIVED_KEY) String messageKey) {
        log.info("messageId : {} and messageKey : {}", messageId, messageKey);
        log.info("Received product created event: {}", event);
        try {
            var success_request_url = request_url + "/200";
            var failure_request_url = request_url + "/500";
            var response = restTemplate.getForEntity(success_request_url, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Received response: {}", response.getBody());
            }
            processedEventRepository.save(new ProcessedEventEntity(messageId, messageKey));
        } catch (ResourceAccessException e) {
            throw new RetryableException(e);
        } catch (Exception e) {
            throw new NotRetryableException(e);
        }
    }
}
