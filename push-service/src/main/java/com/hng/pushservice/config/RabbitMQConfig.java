package com.hng.pushservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    public static final String NOTIFICATIONS_DIRECT_EXCHANGE = "notifications.direct";
    public static final String NOTIFICATIONS_DLX = "notifications.dlx";
    public static final String PUSH_QUEUE = "push.queue";
    public static final String FAILED_PUSH_QUEUE = "push.queue.dlq";
    public static final String PUSH_ROUTING_KEY = "push";

    @Bean
    public DirectExchange notificationsExchange() {
        return new DirectExchange(NOTIFICATIONS_DIRECT_EXCHANGE);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(NOTIFICATIONS_DLX);
    }

    @Bean
    public Queue pushQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", NOTIFICATIONS_DLX);
        args.put("x-dead-letter-routing-key", PUSH_ROUTING_KEY);
        return new Queue(PUSH_QUEUE, true, false, false, args);
    }

    @Bean
    public Queue failedPushQueue() {
        return new Queue(FAILED_PUSH_QUEUE, true);
    }

    @Bean
    public Binding pushBinding(Queue pushQueue, DirectExchange notificationsExchange) {
        return BindingBuilder.bind(pushQueue)
                .to(notificationsExchange)
                .with(PUSH_ROUTING_KEY);
    }

    @Bean
    public Binding failedPushBinding(Queue failedPushQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(failedPushQueue)
                .to(deadLetterExchange)
                .with(PUSH_ROUTING_KEY);
    }

    // ADD THIS METHOD
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, 
                                         Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter converter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(converter);
        return factory;
    }
}