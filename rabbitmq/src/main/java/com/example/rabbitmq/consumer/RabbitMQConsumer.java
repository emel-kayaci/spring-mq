package com.example.rabbitmq.consumer;

import com.example.rabbitmq.exception.TestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQConsumer.class);

    private static final String RETRY_COUNT_HEADER = "retry-count";
    private static final int MAX_RETRY_COUNT = 3;
    private static final long DELAY_TIME = 10000; // 10 seconds delay

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = "main-queue")
    public void consume(Message message) throws TestException {
        String messageBody = new String(message.getBody());
        int retryCount = getRetryCountFromMessage(message);

        LOGGER.info(String.format("(Original) Message -> %s", messageBody));

        if (messageBody.equals("\"error\"")) {
            LOGGER.info("(Original) -> ERROR");
            retryCount++;
            message.getMessageProperties().setHeader(RETRY_COUNT_HEADER, retryCount);
            LOGGER.info(String.format("(Original) Retry Count -> %s", retryCount));

            // Throw an exception to reject and requeue the message with updated retry count
            throw new AmqpRejectAndDontRequeueException("Error! Message should go to DLX and DLQ");
        } else {
            LOGGER.info("(Original) -> Message processed successfully");
        }
    }
    @RabbitListener(queues = "dlq-queue")
    public void consumeDLQ(Message message) throws InterruptedException {
        String messageBody = new String(message.getBody());
        int retryCount = getRetryCountFromMessage(message);

        LOGGER.info(String.format("(DLQ) Message -> %s", messageBody));
        retryCount++;
        message.getMessageProperties().setHeader(RETRY_COUNT_HEADER, retryCount);
        LOGGER.info(String.format("(DLQ) Retry Count -> %s", retryCount));

        if (retryCount > MAX_RETRY_COUNT) {
            LOGGER.warn("Retry count exceeded. Sending message to the parking lot queue.");
            rabbitTemplate.convertAndSend("parking-lot-queue", message);
            return;
        } else {
            // Send the message back to the original queue for retry with a delay
            Thread.sleep(DELAY_TIME);
            rabbitTemplate.convertAndSend("main-queue", message);
        }
    }


    private int getRetryCountFromMessage(Message message) {
        Integer retryCount = (Integer) message.getMessageProperties().getHeaders().getOrDefault(RETRY_COUNT_HEADER, 0);
        return retryCount != null ? retryCount : 0;
    }

}
