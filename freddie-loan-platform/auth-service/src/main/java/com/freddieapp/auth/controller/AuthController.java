package com.freddieapp.auth.controller;

import com.freddieapp.auth.config.EmailNotificationClientConfig;
import com.freddieapp.auth.dto.LoginRequest;
import com.freddieapp.auth.dto.TokenResponse;
import com.freddieapp.auth.dto.UserDto;
import com.freddieapp.auth.service.AuthService;
import com.freddieapp.auth.service.EmailNotificationClientServicer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication Management", description = "Authentication and token management APIs")
public class AuthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    @Autowired
    EmailNotificationClientServicer emailNotificationClientServicer;

    @Autowired
    EmailNotificationClientConfig emailNotificationClientConfig;

    @Value("${bypassPingAuth:false}")
    private String bypassPingAuth;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields();
    }

    @Operation(summary = "Authenticate user credentials and generate JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error") })
    @PostMapping(value = "/login", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        LOGGER.info("OrgAPI: Login User...");
        TokenResponse tokenResponse = authService.login(request);
        if (emailNotificationClientServicer != null) {
            emailNotificationClientServicer.sendEmailNotification(request.getUsername(), "Auth Login Alert", "User authentication successful");
        }
        return ResponseEntity.ok(tokenResponse);
    }

    @Operation(summary = "OAuth2 Token endpoint for password grant type")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error") })
    @PostMapping(value = "/token", consumes = "application/x-www-form-urlencoded", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody ResponseEntity<TokenResponse> tokenOAuth2(
            @RequestParam("username") String username,
            @RequestParam("password") String password) {
        LOGGER.info("OrgAPI: Token OAuth2 Request...");
        LoginRequest request = LoginRequest.builder()
                .username(username)
                .password(password)
                .build();
        TokenResponse tokenResponse = authService.login(request);
        if (emailNotificationClientServicer != null) {
            emailNotificationClientServicer.sendEmailNotification(username, "OAuth2 Token Grant", "OAuth2 Token issued successfully");
        }
        return ResponseEntity.ok(tokenResponse);
    }

    @Operation(summary = "Get current authenticated user profile")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error") })
    @GetMapping(value = "/me", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody ResponseEntity<UserDto> getCurrentUser(@RequestHeader("Authorization") String bearerToken) {
        LOGGER.info("OrgAPI: Get Current User Profile...");
        UserDto user = authService.getUserProfile(bearerToken);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Retrieves JWKS keys for public key verification")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error") })
    @GetMapping(value = "/jwks.json", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody ResponseEntity<Map<String, Object>> getJwks() {
        LOGGER.info("OrgAPI: Get JWKS...");
        return ResponseEntity.ok(authService.getJwks());
    }
}
