package com.freddieapp.loginfrontend.client.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);

    public void notifyLoginFrontendEvent(String user, String status) {
        log.info("[NOTIFICATION-CLIENT] Login Frontend Event: user={}, status={}", user, status);
    }
}
