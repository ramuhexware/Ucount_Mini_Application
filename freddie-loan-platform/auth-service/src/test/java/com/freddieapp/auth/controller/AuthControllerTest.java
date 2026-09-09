package com.freddieapp.auth.controller;

import com.freddieapp.auth.dto.LoginRequest;
import com.freddieapp.auth.dto.TokenResponse;
import com.freddieapp.auth.dto.UserDto;
import com.freddieapp.auth.service.AuthService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private TokenResponse mockTokenResponse;
    private UserDto mockUserDto;

    @Before
    public void setUp() {
        mockTokenResponse = TokenResponse.builder()
                .accessToken("mock-jwt-token-xyz")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .username("officer")
                .fullName("Sarah Jenkins")
                .email("officer@freddiemac.com")
                .roles(Collections.singletonList("LOAN_OFFICER"))
                .build();

        mockUserDto = UserDto.builder()
                .username("officer")
                .fullName("Sarah Jenkins")
                .email("officer@freddiemac.com")
                .roles(Collections.singletonList("LOAN_OFFICER"))
                .build();
    }

    @Test
    public void testLoginEndpointSuccess() {
        when(authService.login(any(LoginRequest.class))).thenReturn(mockTokenResponse);

        LoginRequest request = LoginRequest.builder()
                .username("officer")
                .password("officer123")
                .build();

        ResponseEntity<TokenResponse> response = authController.login(request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("mock-jwt-token-xyz", response.getBody().getAccessToken());
        assertEquals("officer", response.getBody().getUsername());
    }

    @Test
    public void testGetCurrentUserSuccess() {
        when(authService.getUserProfile("Bearer mock-jwt-token-xyz")).thenReturn(mockUserDto);

        ResponseEntity<UserDto> response = authController.getCurrentUser("Bearer mock-jwt-token-xyz");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Sarah Jenkins", response.getBody().getFullName());
    }

    @Test
    public void testGetJwksSuccess() {
        when(authService.getJwks()).thenReturn(Collections.singletonMap("keys", Collections.emptyList()));

        ResponseEntity<Map<String, Object>> response = authController.getJwks();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().containsKey("keys"));
    }
}
