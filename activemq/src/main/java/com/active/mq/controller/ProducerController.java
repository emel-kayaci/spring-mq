package com.active.mq.controller;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Queue;

@RestController
public class ProducerController {
    private final JmsTemplate jmsTemplate;
    private final Queue queue;

    public ProducerController(JmsTemplate jmsTemplate, Queue queue) {
        this.jmsTemplate = jmsTemplate;
        this.queue = queue;
    }

    @PostMapping("/messages")
    public String sendMessage(@RequestBody String message) throws JMSException {
        long ttl = 15000; // Set the TTL value in milliseconds (e.g., 15000ms = 15 seconds)

        jmsTemplate.execute(session -> {
            // Create a JMS message
            Message jmsMessage = session.createTextMessage(message);

            // Calculate the expiration time based on TTL
            long expirationTime = System.currentTimeMillis() + ttl;

            // Set the JMSExpiration property on the message
            jmsMessage.setLongProperty("TTL", ttl);

            // Send the message to the queue
            jmsTemplate.send(queue, s -> jmsMessage);

            return jmsMessage;
        });

        return "Message sent: " + message + " with TTL of " + ttl + "ms";
    }
}