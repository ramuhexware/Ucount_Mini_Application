package com.freddieapp.underwriting.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JMS Publisher Pattern wrapping Spring JmsTemplate for ActiveMQ notification delivery.
 */
@Component
public class NotificationJmsPublisher {

    private static final Logger log = LoggerFactory.getLogger(NotificationJmsPublisher.class);
    private static final String DEFAULT_QUEUE = "FREDDIE-LOAN-NOTIFICATION-QUEUE.local";

    private final JmsTemplate jmsTemplate;

    public record NotificationDTO(String eventId, String eventType, String destination, String payloadJson, LocalDateTime timestamp) {}

    @Autowired
    public NotificationJmsPublisher(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public NotificationDTO publishNotification(String eventType, String destination, String payloadJson) {
        String queueName = (destination != null && !destination.isBlank()) ? destination : DEFAULT_QUEUE;
        String eventId = UUID.randomUUID().toString();

        NotificationDTO dto = new NotificationDTO(eventId, eventType, queueName, payloadJson, LocalDateTime.now());

        try {
            if (jmsTemplate != null) {
                jmsTemplate.convertAndSend(queueName, payloadJson);
                log.info("Published JMS event [{}] to queue [{}]", eventId, queueName);
            }
        } catch (Exception e) {
            log.warn("JMS Broker offline, simulated delivery for event [{}]: {}", eventId, e.getMessage());
        }

        return dto;
    }
}
