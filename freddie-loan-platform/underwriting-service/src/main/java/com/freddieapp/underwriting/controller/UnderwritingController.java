package com.freddieapp.underwriting.controller;

import com.freddieapp.underwriting.config.EmailNotificationClientConfig;
import com.freddieapp.underwriting.dto.UnderwritingRequest;
import com.freddieapp.underwriting.dto.UnderwritingResponse;
import com.freddieapp.underwriting.enums.Decision;
import com.freddieapp.underwriting.service.EmailNotificationClientServicer;
import com.freddieapp.underwriting.service.UnderwritingEngine;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/underwriting")
@Tag(name = "Underwriting & Risk", description = "Risk assessment engines and compliance evaluation APIs")
public class UnderwritingController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnderwritingController.class);

    private final UnderwritingEngine underwritingEngine;

    @Autowired
    EmailNotificationClientServicer emailNotificationClientServicer;

    @Autowired
    EmailNotificationClientConfig emailNotificationClientConfig;

    @Value("${bypassPingAuth:false}")
    private String bypassPingAuth;

    @Autowired
    public UnderwritingController(UnderwritingEngine underwritingEngine) {
        this.underwritingEngine = underwritingEngine;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields();
    }

    @Operation(summary = "Execute automated underwriting assessment rules")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error") })
    @PostMapping(value = "/assess", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody ResponseEntity<UnderwritingResponse> assessLoan(@Valid @RequestBody UnderwritingRequest request) {
        LOGGER.info("OrgAPI: Assess Loan Underwriting...");
        UnderwritingResponse response = underwritingEngine.assessLoan(request);
        if (emailNotificationClientServicer != null) {
            emailNotificationClientServicer.sendEmailNotification("underwriting-alerts@freddiemac.com", 
                    "Underwriting Assessment Complete", "Assessment ID " + response.getAssessmentId() + " decision: " + response.getDecision());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Manual underwriting decision override (Audited)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error") })
    @PostMapping(value = "/override/{assessmentId}", produces = "application/json")
    @PreAuthorize("hasRole('UNDERWRITER')")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody ResponseEntity<UnderwritingResponse> overrideDecision(
            @PathVariable String assessmentId,
            @RequestParam Decision decision,
            @RequestParam String reason,
            @RequestParam String underwriter) {
        LOGGER.info("OrgAPI: Override Underwriting Decision...");
        UnderwritingResponse response = underwritingEngine.overrideDecision(assessmentId, decision, reason, underwriter);
        if (emailNotificationClientServicer != null) {
            emailNotificationClientServicer.sendEmailNotification("compliance@freddiemac.com", 
                    "Underwriting Decision Override", "Assessment ID " + assessmentId + " overridden by " + underwriter);
        }
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get latest risk assessment by loan ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error") })
    @GetMapping(value = "/loan/{loanId}", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody ResponseEntity<UnderwritingResponse> getLatestAssessment(@PathVariable String loanId) {
        LOGGER.info("OrgAPI: Get Latest Assessment...");
        UnderwritingResponse response = underwritingEngine.getLatestAssessment(loanId);
        return response != null ? ResponseEntity.ok(response) : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Get all historical assessments for a customer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error") })
    @GetMapping(value = "/customer/{customerId}", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody ResponseEntity<Page<UnderwritingResponse>> getCustomerAssessments(
            @PathVariable String customerId, Pageable pageable) {
        LOGGER.info("OrgAPI: Get Customer Assessments...");
        Page<UnderwritingResponse> page = underwritingEngine.getCustomerAssessments(customerId, pageable);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Get all underwriting assessments (paginated)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error") })
    @GetMapping(produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody ResponseEntity<Page<UnderwritingResponse>> getAllAssessments(Pageable pageable) {
        LOGGER.info("OrgAPI: Get All Assessments...");
        Page<UnderwritingResponse> page = underwritingEngine.getAllAssessments(pageable);
        return ResponseEntity.ok(page);
    }
}

