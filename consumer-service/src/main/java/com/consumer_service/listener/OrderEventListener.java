package com.consumer_service.listener;
import com.consumer_service.dto.OrderEvent;
import com.consumer_service.service.OrderProcessorService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final OrderProcessorService processorService;

    @RabbitListener(queues = "${messaging.queue.name}", containerFactory = "rabbitListenerContainerFactory")
    public void handleOrderEvent(OrderEvent event, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        
        try {
            log.debug("Received order event: {}", event.getOrderId());
            processorService.processOrder(event);
            
            // Manual acknowledgment - critical for production reliability
            channel.basicAck(deliveryTag, false);
            log.debug("Acknowledged order: {}", event.getOrderId());
            
        } catch (Exception ex) {
            log.error("Failed to process order [{}]: {}", event.getOrderId(), ex.getMessage());
            
            // Negative acknowledgment without requeue (sends to DLQ if configured, else drops)
            // For this setup, we requeue once then dead-letter
            boolean requeue = !message.getMessageProperties().isRedelivered();
            channel.basicNack(deliveryTag, false, requeue);
        }
    }
}
