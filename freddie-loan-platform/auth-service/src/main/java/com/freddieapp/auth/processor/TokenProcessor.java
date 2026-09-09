package com.freddieapp.auth.processor;

import org.springframework.stereotype.Component;

@Component
public class TokenProcessor {

    public String sanitizeUsername(String username) {
        return username != null ? username.trim().toLowerCase() : "";
    }
}
