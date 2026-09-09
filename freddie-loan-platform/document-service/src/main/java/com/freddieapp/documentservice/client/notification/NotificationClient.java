package com.freddieapp.documentservice.client.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);

    public void notifyDocumentUpload(String documentId, String loanId, String documentType) {
        log.info("[NOTIFICATION-CLIENT] Dispatching Document Notification: documentId={}, loanId={}, type={}", documentId, loanId, documentType);
    }
}
