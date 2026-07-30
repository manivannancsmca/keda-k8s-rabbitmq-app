package com.producer_service.config;

import java.time.Instant;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.producer_service.dto.OrderEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderProducerService {

    private final RabbitTemplate rabbitTemplate;

     @Value("${messaging.exchange.name}")
    private String exchangeName;

    @Value("${messaging.routing.key}")
    private String routingKey;

    @Transactional
    public void publishOrder(OrderEvent event) {
        try {
            if (event.getOrderId() == null) {
                event.setOrderId(UUID.randomUUID().toString());
            }
            if (event.getTimestamp() == null) {
                event.setTimestamp(Instant.now());
            }

            rabbitTemplate.convertAndSend(exchangeName, routingKey, event);
            log.info("Published order event: id={}, product={}, qty={}", 
                event.getOrderId(), event.getProductName(), event.getQuantity());
        } catch (Exception ex) {
            log.error("Failed to publish order: {}", ex.getMessage(), ex);
            throw new RuntimeException("Message publication failed", ex);
        }
    }

    public void publishBulkOrders(int count) {
        log.info("Publishing {} bulk orders", count);
        for (int i = 0; i < count; i++) {
            OrderEvent event = OrderEvent.builder()
                .orderId(UUID.randomUUID().toString())
                .productName("Product-" + i)
                .quantity((i % 10) + 1)
                .timestamp(Instant.now())
                .build();
            publishOrder(event);
        }
    }
    
}
