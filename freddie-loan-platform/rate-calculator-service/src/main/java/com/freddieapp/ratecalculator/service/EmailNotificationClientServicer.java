package com.freddieapp.ratecalculator.service;

import com.freddieapp.ratecalculator.config.EmailNotificationClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationClientServicer {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailNotificationClientServicer.class);

    private final EmailNotificationClientConfig emailNotificationClientConfig;

    @Autowired
    public EmailNotificationClientServicer(EmailNotificationClientConfig emailNotificationClientConfig) {
        this.emailNotificationClientConfig = emailNotificationClientConfig;
    }

    public boolean sendEmailNotification(String recipient, String subject, String content) {
        if (!emailNotificationClientConfig.isEnabled()) {
            LOGGER.info("Email notification disabled. Skipping dispatch to {}", recipient);
            return false;
        }
        LOGGER.info("OrgAPI: Dispatching Email Notification to {} | Subject: {} | Host: {}", 
                recipient, subject, emailNotificationClientConfig.getHost());
        return true;
    }
}
