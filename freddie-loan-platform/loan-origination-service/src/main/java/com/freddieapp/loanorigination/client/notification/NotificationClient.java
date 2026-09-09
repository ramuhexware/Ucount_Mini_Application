package com.freddieapp.loanorigination.client.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);

    public void notifyLoanStatusChange(String loanId, String customerId, String newStatus) {
        log.info("[NOTIFICATION-CLIENT] Dispatching Loan Notification: loanId={}, customerId={}, status={}", loanId, customerId, newStatus);
    }
}
