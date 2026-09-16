package com.freddieapp.underwriting.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @PostMapping("/underwriting-token")
    public ResponseEntity<Map<String, String>> generateToken(@RequestParam String username) {
        String token = "underwriting_bearer_jwt_" + System.currentTimeMillis();
        return ResponseEntity.ok(Map.of("token", token, "type", "Bearer", "user", username, "role", "SENIOR_UNDERWRITER"));
    }
}
