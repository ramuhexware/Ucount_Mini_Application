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
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final TokenProcessor tokenProcessor;
    private final NotificationClient notificationClient;
    private final Map<String, UserDto> userStore = new ConcurrentHashMap<>();
    private final Map<String, String> passwordStore = new ConcurrentHashMap<>();

    @Autowired
    public AuthService(JwtTokenProvider tokenProvider, UserRepository userRepository, TokenProcessor tokenProcessor, NotificationClient notificationClient) {
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.tokenProcessor = tokenProcessor;
        this.notificationClient = notificationClient;
        initUsers();
    }

    private void initUsers() {
        // Preset users for mortgage platform role-based testing
        addUser("USR-001", "admin", "admin123", "System Administrator", "admin@freddiemac.com",
                List.of("ADMIN", "LOAN_OFFICER", "UNDERWRITER", "CUSTOMER"));

        addUser("USR-002", "officer", "officer123", "Sarah Jenkins (Loan Officer)", "officer@freddiemac.com",
                List.of("LOAN_OFFICER", "CUSTOMER"));

        addUser("USR-003", "underwriter", "underwriter123", "Michael Vance (Senior Underwriter)", "underwriter@freddiemac.com",
                List.of("UNDERWRITER", "CUSTOMER"));

        addUser("USR-004", "customer", "customer123", "John Doe (Borrower)", "john.doe@example.com",
                List.of("CUSTOMER"));
    }

    private void addUser(String id, String username, String password, String fullName, String email, List<String> roles) {
        UserDto user = UserDto.builder()
                .id(id)
                .username(username)
                .email(email)
                .fullName(fullName)
                .roles(roles)
                .active(true)
                .build();
        userStore.put(username, user);
        passwordStore.put(username, password);
    }

    public TokenResponse login(LoginRequest request) {
        UserDto user = userStore.get(request.getUsername());
        if (user == null || !request.getPassword().equals(passwordStore.get(request.getUsername()))) {
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
        UserDto user = userStore.get(username);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        return user;
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
