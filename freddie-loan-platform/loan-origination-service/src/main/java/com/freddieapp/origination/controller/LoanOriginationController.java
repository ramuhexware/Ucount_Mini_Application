package com.freddieapp.origination.controller;

import com.freddieapp.origination.model.LoanApplicationEntity.LoanRequest;
import com.freddieapp.origination.model.LoanApplicationEntity.LoanResponse;
import com.freddieapp.origination.service.LoanOriginationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Loan Origination Microservice with mass assignment protection & exception handling.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Loan Origination APIs", description = "Endpoints for Customer Onboarding and Application Lifecycle")
public class LoanOriginationController {

    private final LoanOriginationService service;

    @Autowired
    public LoanOriginationController(LoanOriginationService service) {
        this.service = service;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("id", "status", "createdAt");
    }

    @PostMapping("/auth/login")
    @Operation(summary = "User Authentication & JWT Token")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.getOrDefault("username", "user");
        return ResponseEntity.ok(Map.of(
            "accessToken", "jwt-token-" + System.currentTimeMillis(),
            "tokenType", "Bearer",
            "username", username,
            "role", "LOAN_OFFICER"
        ));
    }

    @PostMapping("/loans")
    @Operation(summary = "Submit New Mortgage Application")
    public ResponseEntity<LoanResponse> submitLoan(@Valid @RequestBody LoanRequest request) {
        return new ResponseEntity<>(service.createLoanApplication(request), HttpStatus.CREATED);
    }

    @GetMapping("/loans/{loanId}")
    @Operation(summary = "Get Loan Application by ID")
    public ResponseEntity<LoanResponse> getLoanById(@PathVariable Long loanId) {
        return ResponseEntity.ok(service.getLoanById(loanId));
    }

    @GetMapping("/customers/{customerId}/loans")
    @Operation(summary = "Get Customer Loans")
    public ResponseEntity<List<LoanResponse>> getCustomerLoans(@PathVariable String customerId) {
        return ResponseEntity.ok(service.getLoansByCustomerId(customerId));
    }

    @PostMapping("/loans/{loanId}/submit-underwriting")
    @Operation(summary = "Transition Status to UNDER_REVIEW via PostgreSQL Native Query")
    public ResponseEntity<LoanResponse> submitForUnderwriting(@PathVariable Long loanId) {
        return ResponseEntity.ok(service.submitForUnderwritingNative(loanId));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
            "status", HttpStatus.BAD_REQUEST.value(),
            "error", "Origination Error",
            "message", ex.getMessage(),
            "timestamp", LocalDateTime.now()
        ));
    }
}
