package com.active.mq.config;

import jakarta.jms.Queue;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.jms.ConnectionFactory;

@Configuration
public class ActiveMQConfig {

    @Bean
    public RedeliveryPolicy redeliveryPolicy() {
        RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
        redeliveryPolicy.setInitialRedeliveryDelay(2000); // Delay before first redelivery attempt (in milliseconds)
        redeliveryPolicy.setRedeliveryDelay(1000); // Delay between subsequent redelivery attempts (in milliseconds)
        redeliveryPolicy.setMaximumRedeliveries(3); // Maximum number of redelivery attempts
        redeliveryPolicy.setUseExponentialBackOff(false); // Disable exponential backoff
        redeliveryPolicy.setDestination(new ActiveMQQueue("DLQ")); // Specify the DLQ
        return redeliveryPolicy;
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
        connectionFactory.setBrokerURL("tcp://localhost:61616");
        connectionFactory.setTrustAllPackages(true);
        connectionFactory.setRedeliveryPolicy(redeliveryPolicy()); // Set the RedeliveryPolicy
        return connectionFactory;
    }

    @Bean
    public Queue queue() {
        return new ActiveMQQueue("queue.sample");
    }

    @Bean
    public Queue dlq() {
        return new ActiveMQQueue("DLQ");
    }

    @Bean
    public Queue expiredQueue() {
        return new ActiveMQQueue("expired");
    }
}