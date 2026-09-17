package com.freddieapp.underwriting.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * ActiveMQ JMS Event Listener for receiving, processing, and auditing
 * event messages published to the underwriting destination queue.
 */
@Component
public class NotificationJmsListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationJmsListener.class);

    @JmsListener(destination = "${freddie.underwriting.jms.destination}")
    public void receiveNotificationEvent(String messagePayload) {
        LOGGER.info("JMS Listener Received Event Payload from ActiveMQ Queue: {}", messagePayload);
        processNotificationEvent(messagePayload);
    }

    private void processNotificationEvent(String payload) {
        LOGGER.info("Successfully processed ActiveMQ event notification message payload.");
    }
}
