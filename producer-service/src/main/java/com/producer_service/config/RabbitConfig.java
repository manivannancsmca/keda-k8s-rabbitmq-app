package com.producer_service.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.transaction.RabbitTransactionManager;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RabbitConfig {

    @Value("${messaging.exchange.name}")
    private String exchangeName;

    @Value("${messaging.exchange.type}")
    private String exchangeType;

    @Value("${messaging.exchange.durable}")
    private boolean exchangeDurable;

    @Value("${messaging.queue.name}")
    private String queueName;

    @Value("${messaging.queue.durable}")
    private boolean queueDurable;

    @Value("${messaging.routing.key}")
    private String routingKey;

    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(exchangeName, exchangeDurable, false);
    }

    @Bean
    public Queue orderQueue() {
        return new Queue(queueName, queueDurable, false, false);
    }

    @Bean
    public Binding orderBinding(Queue orderQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(orderQueue)
                .to(orderExchange)
                .with(routingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        template.setMandatory(true);
        template.setConfirmCallback((correlation, ack, reason) -> {
            if (!ack) {
                // Log or handle nack - production would use retry/dead-letter
                System.err.println("Message nack: " + reason);
            }
        });
        return template;
    }

    @Bean
    public RabbitTransactionManager rabbitTransactionManager(ConnectionFactory connectionFactory) {
        return new RabbitTransactionManager(connectionFactory);
    }
}
