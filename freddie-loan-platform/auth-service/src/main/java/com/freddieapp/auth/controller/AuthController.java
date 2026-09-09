package com.freddieapp.auth.controller;

import com.freddieapp.auth.dto.LoginRequest;
import com.freddieapp.auth.dto.TokenResponse;
import com.freddieapp.auth.dto.UserDto;
import com.freddieapp.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse tokenResponse = authService.login(request);
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping(value = "/token", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity<TokenResponse> tokenOAuth2(
            @RequestParam("username") String username,
            @RequestParam("password") String password) {
        LoginRequest request = LoginRequest.builder()
                .username(username)
                .password(password)
                .build();
        TokenResponse tokenResponse = authService.login(request);
        return ResponseEntity.ok(tokenResponse);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(@RequestHeader("Authorization") String bearerToken) {
        UserDto user = authService.getUserProfile(bearerToken);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/jwks.json")
    public ResponseEntity<Map<String, Object>> getJwks() {
        return ResponseEntity.ok(authService.getJwks());
    }
}
