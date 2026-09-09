package com.freddieapp.loginfrontend.processor;

import org.springframework.stereotype.Component;

@Component
public class LoginProcessor {

    public String sanitizeUsername(String username) {
        return username != null ? username.trim().toLowerCase() : "";
    }
}
