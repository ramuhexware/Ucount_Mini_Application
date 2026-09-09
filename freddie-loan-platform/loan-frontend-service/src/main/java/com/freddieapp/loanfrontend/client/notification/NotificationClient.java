package com.freddieapp.loanfrontend.client.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);

    public void notifyLoanFrontendEvent(String loanId, String status) {
        log.info("[NOTIFICATION-CLIENT] Loan Frontend Event: loanId={}, status={}", loanId, status);
    }
}
