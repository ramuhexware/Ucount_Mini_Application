package com.freddieapp.ratecalculator.client.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);

    public void notifyRateCalculation(double calculatedRate, int creditScore) {
        log.info("[NOTIFICATION-CLIENT] Rate Calculated: rate={}, FICO={}", calculatedRate, creditScore);
    }
}
