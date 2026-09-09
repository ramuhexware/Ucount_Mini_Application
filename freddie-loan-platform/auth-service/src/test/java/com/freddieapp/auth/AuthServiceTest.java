package com.freddieapp.auth;

import com.freddieapp.auth.config.JwtTokenProvider;
import com.freddieapp.auth.dto.LoginRequest;
import com.freddieapp.auth.dto.TokenResponse;
import com.freddieapp.auth.dto.UserDto;
import com.freddieapp.auth.service.AuthService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.*;

public class AuthServiceTest {

    private JwtTokenProvider jwtTokenProvider;
    private AuthService authService;

    @Before
    public void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "secretKey", "FreddieMacOAuth2SuperSecretKeyForJWTTokenGeneration2026Secure");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationInMs", 86400000L);
        ReflectionTestUtils.setField(jwtTokenProvider, "issuer", "http://localhost:8080/realms/freddie-platform");

        authService = new AuthService(jwtTokenProvider, new com.freddieapp.auth.repository.UserRepository(), new com.freddieapp.auth.processor.TokenProcessor(), new com.freddieapp.auth.client.notification.NotificationClient());
    }

    @Test
    public void testLoginSuccess() {
        LoginRequest request = LoginRequest.builder()
                .username("officer")
                .password("officer123")
                .build();

        TokenResponse response = authService.login(request);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("officer", response.getUsername());
        assertTrue(response.getRoles().contains("LOAN_OFFICER"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoginInvalidPassword() {
        LoginRequest request = LoginRequest.builder()
                .username("officer")
                .password("wrongpassword")
                .build();

        authService.login(request);
    }

    @Test
    public void testGetUserProfile() {
        LoginRequest request = LoginRequest.builder()
                .username("admin")
                .password("admin123")
                .build();
        TokenResponse tokenResponse = authService.login(request);

        UserDto userProfile = authService.getUserProfile("Bearer " + tokenResponse.getAccessToken());

        assertNotNull(userProfile);
        assertEquals("admin", userProfile.getUsername());
        assertTrue(userProfile.getRoles().contains("ADMIN"));
    }
}
