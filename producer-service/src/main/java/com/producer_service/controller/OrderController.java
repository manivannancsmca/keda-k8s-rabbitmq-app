package com.producer_service.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.producer_service.config.OrderProducerService;
import com.producer_service.dto.OrderEvent;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

     private final OrderProducerService producerService;

     public ResponseEntity<Map<String, String>> createOrder(@Valid @RequestBody OrderEvent event) {
        producerService.publishOrder(event);

        return ResponseEntity.accepted()
            .body(Map.of(
                "status", "ACCEPTED",
                "message", "Order event published successfully",
                "orderId", event.getOrderId()
            ));
     }

     @PostMapping("/bulk/{count}")
    public ResponseEntity<Map<String, String>> createBulkOrders(@PathVariable int count) {
        producerService.publishBulkOrders(count);
        return ResponseEntity.accepted()
            .body(Map.of(
                "status", "ACCEPTED",
                "message", count + " order events published successfully"
            ));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "producer"));
    }
}
