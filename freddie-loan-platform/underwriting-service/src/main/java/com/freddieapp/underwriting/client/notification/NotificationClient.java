package com.freddieapp.underwriting.client.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);

    public void notifyUnderwritingDecision(String assessmentId, String loanId, String decision) {
        log.info("[NOTIFICATION-CLIENT] Dispatching Underwriting Decision: assessmentId={}, loanId={}, decision={}", assessmentId, loanId, decision);
    }
}
