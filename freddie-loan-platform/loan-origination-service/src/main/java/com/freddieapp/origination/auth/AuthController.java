package com.freddieapp.origination.auth;

import com.freddieapp.origination.dto.AuthResponseDTO;
import com.freddieapp.origination.dto.LoginRequestDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTO loginRequest) {
        String token = "mock_jwt_bearer_token_" + System.currentTimeMillis();
        AuthResponseDTO response = new AuthResponseDTO(token, "Bearer", loginRequest.username(), "LOAN_OFFICER");
        return ResponseEntity.ok(response);
    }
}
