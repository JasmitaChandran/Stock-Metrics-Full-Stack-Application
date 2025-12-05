package com.stock.metrics.order.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.stock.metrics.order.events.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.orders}")
    private String ordersTopic;

    public OrderEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;

        // Create and configure ObjectMapper locally (no Spring bean needed)
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public void publish(OrderEvent event) {
        try {
            String key = event.getSymbol() != null ? event.getSymbol().toUpperCase() : "UNKNOWN";
            String payload = objectMapper.writeValueAsString(event);

            kafkaTemplate.send(ordersTopic, key, payload)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to send order event for orderId={}: {}", event.getOrderId(), ex.getMessage());
                        } else {
                            log.info("Published order event for orderId={} to topic {} partition {}",
                                    event.getOrderId(),
                                    ordersTopic,
                                    result.getRecordMetadata().partition());
                        }
                    });
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize OrderEvent for orderId={}: {}", event.getOrderId(), e.getMessage());
        }
    }
}
