package com.freddieapp.origination.controller;

import com.freddieapp.origination.model.LoanApplicationEntity.*;
import com.freddieapp.origination.service.LoanOriginationService;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Loan Origination, Stage 1/2 Onboarding, and Account Services.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Loan Origination & Account APIs", description = "Endpoints for Onboarding, Stage 1/2 Profiles, and Account Services")
public class LoanOriginationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoanOriginationController.class);

    private final LoanOriginationService service;

    @Value("${bypassPingAuth:false}")
    private String bypassPingAuth;

    @Autowired
    public LoanOriginationController(LoanOriginationService service) {
        this.service = service;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields();
    }

    // Lookup Endpoint
    @Operation(summary = "Retrieves the reference tables for create account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping(value = "/lookup", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody AccountLookupDTO lookup() {
        LOGGER.info("OrgAPI: Lookup Account...");
        return service.getLookupData();
    }

    // Account Lookup Update Endpoint (Matching code image)
    @Operation(summary = "Retrieves the account lookup update data with product and org role mappings")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping(value = "/lookup/update", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody AccountLookupUpdateDTO lookupUpdate() {
        LOGGER.info("Org API: Account lookup update data is retrieved successfully.");
        return service.getLookupUpdateData();
    }

    // Create Account Endpoint
    @Operation(summary = "Create Account object and persist it to Database")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @PostMapping(value = "/create", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody AccountSaveDTO createAccount(@RequestBody AccountSaveDTO accountReq) {
        LOGGER.info("OrgAPI: Create Account...");
        AccountSaveDTO account = service.createAccount(accountReq);

        boolean bypassingPing = Boolean.parseBoolean(bypassPingAuth);

        if (null != account
            && HttpStatus.OK.value() == account.getRespSts().getStatusCode().value()
            && !bypassingPing) {

            AccountProfileReqDTO accountProfileReqDTO = new AccountProfileReqDTO(accountReq.getIdCntprtyAcct());
            AccountProfileRespDTO accountProfile = service.getAccountProfile(accountProfileReqDTO);
            LOGGER.info("OrgAPI: Verified Account Profile: {}", accountProfile.profileStatus());
        }

        return account;
    }

    // Auth Login Endpoint
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

    // Stage 1 Intake APIs
    @PostMapping("/stage1/onboard")
    @Operation(summary = "Stage 1 Intake: Counterparty Initial Registration")
    public ResponseEntity<Stage1UserResponse> onboardStage1(@Valid @RequestBody Stage1OnboardRequest request) {
        return new ResponseEntity<>(service.onboardStage1User(request), HttpStatus.CREATED);
    }

    @PostMapping("/stage1/approve/{userId}")
    @Operation(summary = "Stage 1 Intake: Approve Counterparty Registration")
    public ResponseEntity<Stage1UserResponse> approveStage1(@PathVariable String userId) {
        return ResponseEntity.ok(service.approveStage1User(userId));
    }

    @GetMapping("/stage1/pending")
    @Operation(summary = "Stage 1 Intake: Get Pending Counterparty Applications")
    public ResponseEntity<List<Stage1UserResponse>> getPendingStage1() {
        return ResponseEntity.ok(service.getPendingStage1Users());
    }

    // Stage 2 Extended Profile & Access Rights APIs
    @PostMapping("/stage2/profiles")
    @Operation(summary = "Stage 2 Profile: Register Extended Profile & Access Rights")
    public ResponseEntity<Stage2AccessRightsResponse> saveStage2Profile(@Valid @RequestBody Stage2ProfileRequest request) {
        return new ResponseEntity<>(service.saveStage2Profile(request), HttpStatus.CREATED);
    }

    @GetMapping("/stage2/access-rights/{userId}")
    @Operation(summary = "Stage 2 Profile: Get Access Rights by User ID")
    public ResponseEntity<Stage2AccessRightsResponse> getStage2AccessRights(@PathVariable String userId) {
        return ResponseEntity.ok(service.getStage2AccessRights(userId));
    }

    // Loan Origination APIs
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
