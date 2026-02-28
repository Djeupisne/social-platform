package com.social.main.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String MENAGE_EXCHANGE = "menage.exchange";
    public static final String MENAGE_CREATED_QUEUE = "menage.created.queue";
    public static final String MENAGE_UPDATED_QUEUE = "menage.updated.queue";
    public static final String MENAGE_CREATED_KEY = "menage.created";
    public static final String MENAGE_UPDATED_KEY = "menage.updated";

    @Bean
    public TopicExchange menageExchange() {
        return new TopicExchange(MENAGE_EXCHANGE);
    }

    @Bean
    public Queue menageCreatedQueue() {
        return QueueBuilder.durable(MENAGE_CREATED_QUEUE).build();
    }

    @Bean
    public Queue menageUpdatedQueue() {
        return QueueBuilder.durable(MENAGE_UPDATED_QUEUE).build();
    }

    @Bean
    public Binding menageCreatedBinding() {
        return BindingBuilder.bind(menageCreatedQueue())
                .to(menageExchange())
                .with(MENAGE_CREATED_KEY);
    }

    @Bean
    public Binding menageUpdatedBinding() {
        return BindingBuilder.bind(menageUpdatedQueue())
                .to(menageExchange())
                .with(MENAGE_UPDATED_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory factory) {
        RabbitTemplate template = new RabbitTemplate(factory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}