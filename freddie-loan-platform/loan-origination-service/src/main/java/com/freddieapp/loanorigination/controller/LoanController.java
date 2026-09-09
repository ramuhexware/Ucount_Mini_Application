package com.freddieapp.loanorigination.controller;

import com.freddieapp.loanorigination.config.EmailNotificationClientConfig;
import com.freddieapp.loanorigination.dto.LoanApplicationRequest;
import com.freddieapp.loanorigination.dto.LoanApplicationResponse;
import com.freddieapp.loanorigination.service.EmailNotificationClientServicer;
import com.freddieapp.loanorigination.service.LoanOriginationService;
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
@RequestMapping("/api/v1/loans")
@Tag(name = "Loan Origination", description = "Loan application lifecycle management APIs")
public class LoanController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoanController.class);

    private final LoanOriginationService loanOriginationService;

    @Autowired
    EmailNotificationClientServicer emailNotificationClientServicer;

    @Autowired
    EmailNotificationClientConfig emailNotificationClientConfig;

    @Value("${bypassPingAuth:false}")
    private String bypassPingAuth;

    @Autowired
    public LoanController(LoanOriginationService loanOriginationService) {
        this.loanOriginationService = loanOriginationService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields();
    }

    @Operation(summary = "Submit a new loan application")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error") })
    @PostMapping(consumes = "application/json", produces = "application/json")
    @PreAuthorize("hasAnyRole('LOAN_OFFICER', 'ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody ResponseEntity<LoanApplicationResponse> submitLoanApplication(
            @Valid @RequestBody LoanApplicationRequest request) {
        LOGGER.info("OrgAPI: Submit Loan Application...");
        LoanApplicationResponse response = loanOriginationService.submitLoanApplication(request);
        if (emailNotificationClientServicer != null) {
            emailNotificationClientServicer.sendEmailNotification("applicant@freddiemac.com", 
                    "Loan Application Submitted", "Application ID: " + response.getLoanId() + " has been created.");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get loan application by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error") })
    @GetMapping(value = "/{loanId}", produces = "application/json")
    @PreAuthorize("hasAnyRole('LOAN_OFFICER', 'UNDERWRITER', 'ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody ResponseEntity<LoanApplicationResponse> getLoanById(@PathVariable String loanId) {
        LOGGER.info("OrgAPI: Get Loan By ID...");
        return ResponseEntity.ok(loanOriginationService.getLoanById(loanId));
    }

    @Operation(summary = "Get all loans for a customer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error") })
    @GetMapping(value = "/customer/{customerId}", produces = "application/json")
    @PreAuthorize("hasAnyRole('LOAN_OFFICER', 'ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody ResponseEntity<Page<LoanApplicationResponse>> getLoansByCustomer(
            @PathVariable String customerId, Pageable pageable) {
        LOGGER.info("OrgAPI: Get Loans By Customer...");
        return ResponseEntity.ok(loanOriginationService.getLoansByCustomer(customerId, pageable));
    }

    @Operation(summary = "Submit loan to underwriting service")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error") })
    @PostMapping(value = "/{loanId}/submit-for-underwriting", produces = "application/json")
    @PreAuthorize("hasAnyRole('LOAN_OFFICER', 'ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody ResponseEntity<LoanApplicationResponse> submitForUnderwriting(@PathVariable String loanId) {
        LOGGER.info("OrgAPI: Submit Loan to Underwriting...");
        LoanApplicationResponse response = loanOriginationService.submitForUnderwriting(loanId);
        if (emailNotificationClientServicer != null) {
            emailNotificationClientServicer.sendEmailNotification("underwriting@freddiemac.com", 
                    "Loan Submitted to Underwriting", "Loan ID " + loanId + " submitted for risk evaluation.");
        }
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all loan applications (paginated)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error") })
    @GetMapping(produces = "application/json")
    @PreAuthorize("hasAnyRole('LOAN_OFFICER', 'UNDERWRITER', 'ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody ResponseEntity<Page<LoanApplicationResponse>> getAllLoans(Pageable pageable) {
        LOGGER.info("OrgAPI: Get All Loans...");
        return ResponseEntity.ok(loanOriginationService.getAllLoans(pageable));
    }
}
