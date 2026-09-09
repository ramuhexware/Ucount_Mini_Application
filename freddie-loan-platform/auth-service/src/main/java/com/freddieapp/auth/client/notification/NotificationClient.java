package com.freddieapp.auth.client.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);

    public void notifyLoginEvent(String username, boolean success) {
        log.info("[NOTIFICATION-CLIENT] Dispatching Login Event Notification: user={}, success={}", username, success);
    }
}
