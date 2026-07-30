package com.consumer_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.consumer_service.dto.OrderEvent;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
public class OrderProcessorService {

    public void processOrder(OrderEvent event) {
        Instant start = Instant.now();
        
        try {
            // Simulate business processing (100-500ms)
            long processingTime = 100 + (long)(Math.random() * 400);
            Thread.sleep(processingTime);

            // Simulate 5% failure rate for realistic retry testing
            if (Math.random() < 0.05) {
                throw new RuntimeException("Simulated processing failure for order: " + event.getOrderId());
            }

            Duration duration = Duration.between(start, Instant.now());
            log.info("Processed order [{}] in {}ms | Product: {} | Qty: {}",
                event.getOrderId(),
                duration.toMillis(),
                event.getProductName(),
                event.getQuantity()
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Processing interrupted", e);
        }
    }
}