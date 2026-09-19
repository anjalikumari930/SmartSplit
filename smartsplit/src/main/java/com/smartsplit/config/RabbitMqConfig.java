package com.smartsplit.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE_NAME = "smartsplit.exchange";
    public static final String NOTIFICATION_QUEUE = "smartsplit.notification.queue";
    public static final String NOTIFICATION_ROUTING_KEY = "smartsplit.notification";
    public static final String NOTIFICATION_DLX = "smartsplit.notification.dlx";
    public static final String NOTIFICATION_DLQ = "smartsplit.notification.dlq";
    public static final String NOTIFICATION_DLQ_ROUTING_KEY = "smartsplit.notification.dlq";

    @Bean
    public TopicExchange smartsplitExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", NOTIFICATION_DLX)
                .withArgument("x-dead-letter-routing-key", NOTIFICATION_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public TopicExchange notificationDlx() {
        return new TopicExchange(NOTIFICATION_DLX);
    }

    @Bean
    public Queue notificationDlq() {
        return QueueBuilder.durable(NOTIFICATION_DLQ).build();
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(smartsplitExchange())
                .with(NOTIFICATION_ROUTING_KEY);
    }

    @Bean
    public Binding notificationDlqBinding() {
        return BindingBuilder
                .bind(notificationDlq())
                .to(notificationDlx())
                .with(NOTIFICATION_DLQ_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
