package com.freddieapp.loginfrontend.service;

import com.freddieapp.loginfrontend.client.notification.NotificationClient;
import com.freddieapp.loginfrontend.enums.LoginState;
import com.freddieapp.loginfrontend.processor.LoginProcessor;
import com.freddieapp.loginfrontend.repository.LoginAuditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoginFrontendService {

    private final LoginAuditRepository repository;
    private final LoginProcessor processor;
    private final NotificationClient notificationClient;

    @Autowired
    public LoginFrontendService(LoginAuditRepository repository, LoginProcessor processor, NotificationClient notificationClient) {
        this.repository = repository;
        this.processor = processor;
        this.notificationClient = notificationClient;
    }

    public void processLoginAudit(String username, boolean success) {
        String cleanUser = processor.sanitizeUsername(username);
        LoginState state = success ? LoginState.SUCCESS : LoginState.FAILED;
        repository.recordLoginAttempt(cleanUser, state);
        notificationClient.notifyLoginFrontendEvent(cleanUser, state.name());
    }
}
