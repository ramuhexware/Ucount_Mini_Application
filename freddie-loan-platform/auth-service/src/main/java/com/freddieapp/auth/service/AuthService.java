package com.freddieapp.auth.service;

import com.freddieapp.auth.client.notification.NotificationClient;
import com.freddieapp.auth.config.JwtTokenProvider;
import com.freddieapp.auth.dto.LoginRequest;
import com.freddieapp.auth.dto.TokenResponse;
import com.freddieapp.auth.dto.UserDto;
import com.freddieapp.auth.processor.TokenProcessor;
import com.freddieapp.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AuthService {

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final TokenProcessor tokenProcessor;
    private final NotificationClient notificationClient;

    @Autowired
    public AuthService(JwtTokenProvider tokenProvider, UserRepository userRepository, TokenProcessor tokenProcessor, NotificationClient notificationClient) {
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.tokenProcessor = tokenProcessor;
        this.notificationClient = notificationClient;
    }

    public TokenResponse login(LoginRequest request) {
        // Query user via UserRepository ORM method
        UserDto user = userRepository.findUserByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        // Query user password via UserRepository Native Query simulation method
        String expectedPassword = userRepository.findPasswordByUsernameNative(request.getUsername())
                .orElse(null);

        if (expectedPassword == null || !request.getPassword().equals(expectedPassword)) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        String token = tokenProvider.generateToken(user);
        String refreshToken = UUID.randomUUID().toString();

        return TokenResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getExpirationInMs() / 1000)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(user.getRoles())
                .build();
    }

    public UserDto getUserProfile(String bearerToken) {
        String token = extractToken(bearerToken);
        if (!tokenProvider.validateToken(token)) {
            throw new IllegalArgumentException("Invalid or expired token");
        }
        String username = tokenProvider.getUsernameFromToken(token);
        
        // Delegate user profile lookup to UserRepository
        return userRepository.findUserByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public Map<String, Object> getJwks() {
        Map<String, Object> jwk = new HashMap<>();
        jwk.put("kty", "oct");
        jwk.put("use", "sig");
        jwk.put("alg", "HS256");
        jwk.put("kid", "freddie-hmac-key");

        Map<String, Object> response = new HashMap<>();
        response.put("keys", List.of(jwk));
        return response;
    }

    private String extractToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return bearerToken;
    }
}
