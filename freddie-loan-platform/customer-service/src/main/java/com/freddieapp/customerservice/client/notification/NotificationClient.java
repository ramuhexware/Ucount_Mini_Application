package com.freddieapp.customerservice.client.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);

    public void sendCustomerNotification(UUID customerId, String eventType, String message) {
        log.info("[NOTIFICATION-CLIENT] Dispatching event notification to customerId={}: event={}, message={}", customerId, eventType, message);
    }
}
