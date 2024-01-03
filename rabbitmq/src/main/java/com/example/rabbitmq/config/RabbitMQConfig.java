package com.example.rabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // JSON QUEUE

    @Value("${rabbitmq.queue.json.name}")
    private String jsonQueue;

    @Value("${rabbitmq.routing.json.key}")
    private String routingJsonKey;

    @Bean
    public Queue jsonQueue() {
        return new Queue(jsonQueue);
    }

    @Bean
    public Binding jsonBinding() {
        return BindingBuilder.bind(jsonQueue()).to(exchange()).with(routingJsonKey);
    }

    @Bean
    public Queue queue() {
        return QueueBuilder.durable("main-queue")
                .withArgument("x-max-delivery-count", 3)
                .withArgument("x-dead-letter-exchange", "dlq-exchange")
                .withArgument("x-dead-letter-routing-key", "dlq-routing-key")
                .build();
    }

    @Bean
    public Queue dlqQueue() {
        return new Queue("dlq-queue");
    }

    @Bean
    public Queue parkingLotQueue() {
        return new Queue("parking-lot-queue");
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange("main-exchange");
    }

    @Bean
    public TopicExchange dlqExchange() {
        return new TopicExchange("dlq-exchange");
    }

    @Bean
    public TopicExchange parkingLotExchange() {
        return new TopicExchange("parking-lot-exchange");
    }

    @Bean
    public Binding binding() {
        return BindingBuilder.bind(queue()).to(exchange()).with("main-routing-key");
    }

    @Bean
    public Binding dlqBinding() {
        return BindingBuilder.bind(dlqQueue()).to(dlqExchange()).with("dlq-routing-key");
    }

    @Bean
    public Binding parkingLotBinding() {
        return BindingBuilder.bind(parkingLotQueue()).to(parkingLotExchange()).with("parking-lot-routing-key");
    }


    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }
}
