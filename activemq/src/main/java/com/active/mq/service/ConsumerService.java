package com.active.mq.service;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Session;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import jakarta.jms.Queue;


@Service
public class ConsumerService {

    private final Queue dlq;

    private final Queue expiredQueue;


    public ConsumerService(@Qualifier("dlq") Queue dlq, @Qualifier("expiredQueue") Queue expiredQueue) {
        this.dlq = dlq;
        this.expiredQueue = expiredQueue;
    }

    // SIMULATE TTL
    @JmsListener(destination = "queue.sample")
    public void onMessage(Message message, Session session) throws JMSException {
        long ttl = message.getLongProperty("TTL");
        long expirationTime = message.getJMSTimestamp() + ttl;

        try {
            // Simulate a delay longer than the TTL (e.g., 20 seconds)
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (System.currentTimeMillis() >= expirationTime) {
            session.createProducer(expiredQueue).send(message);
            System.out.println("Moved expired message to 'expired' queue: " + message.getJMSMessageID());
            return;
        }

        try {
            System.out.println("Received message: " + message.getBody(String.class));
            throw new RuntimeException("Simulating an exception"); // Uncomment this line to simulate a failed message
        } catch (Exception e) {
            System.err.println("Failed to process message: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
