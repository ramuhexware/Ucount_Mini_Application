package com.freddieapp.origination.cache;

import org.springframework.stereotype.Component;

@Component
public class OAuthTokenCache {
    public String getOAuthAccessToken() {
        return "Bearer sample_oauth_token_12345";
    }
}
