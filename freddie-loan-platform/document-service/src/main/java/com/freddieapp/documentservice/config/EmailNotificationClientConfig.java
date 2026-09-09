package com.freddieapp.documentservice.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class EmailNotificationClientConfig {

    @Value("${email.notification.enabled:true}")
    private boolean enabled;

    @Value("${email.notification.host:smtp.freddiemac.com}")
    private String host;

    @Value("${email.notification.port:587}")
    private int port;

    @Value("${email.notification.from-address:no-reply@freddiemac.com}")
    private String fromAddress;

    @Value("${bypassPingAuth:false}")
    private String bypassPingAuth;
}
