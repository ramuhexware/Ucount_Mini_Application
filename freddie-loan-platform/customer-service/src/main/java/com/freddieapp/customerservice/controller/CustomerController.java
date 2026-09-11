package com.freddieapp.customerservice.controller;

import com.freddieapp.customerservice.config.EmailNotificationClientConfig;
import com.freddieapp.customerservice.dto.CustomerRequest;
import com.freddieapp.customerservice.dto.CustomerResponse;
import com.freddieapp.customerservice.service.CustomerService;
import com.freddieapp.customerservice.service.EmailNotificationClientServicer;
import com.freddieapp.customerservice.client.CardSyncClient;
import com.freddieapp.customerservice.enums.KycStatus;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "Customer Management", description = "Customer profile and KYC management APIs")
public class CustomerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerService customerService;
    private final CardSyncClient cardSyncClient;

    @Autowired
    EmailNotificationClientServicer emailNotificationClientServicer;

    @Autowired
    EmailNotificationClientConfig emailNotificationClientConfig;

    @Value("${bypassPingAuth:false}")
    private String bypassPingAuth;

    @Autowired
    public CustomerController(CustomerService customerService, CardSyncClient cardSyncClient) {
        this.customerService = customerService;
        this.cardSyncClient = cardSyncClient;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields();
    }

    @Operation(summary = "Create new customer account and trigger notification email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error") })
    @PostMapping(consumes = "application/json", produces = "application/json")
    @PreAuthorize("hasAnyRole('LOAN_OFFICER', 'ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerRequest request) {
        LOGGER.info("OrgAPI: Create Customer...");
        CustomerResponse response = customerService.createCustomer(request);
        if (emailNotificationClientServicer != null) {
            emailNotificationClientServicer.sendEmailNotification(request.getEmail(), "Customer Account Created", "Welcome to Freddie Mac Mortgage Platform");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get customer profile by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error") })
    @GetMapping(value = "/{customerId}", produces = "application/json")
    @PreAuthorize("hasAnyRole('LOAN_OFFICER', 'UNDERWRITER', 'ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody ResponseEntity<CustomerResponse> getCustomerById(@PathVariable UUID customerId) {
        LOGGER.info("OrgAPI: Get Customer By ID...");
        return ResponseEntity.ok(customerService.getCustomerById(customerId));
    }

    @Operation(summary = "Update customer profile")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error") })
    @PutMapping(value = "/{customerId}", consumes = "application/json", produces = "application/json")
    @PreAuthorize("hasAnyRole('LOAN_OFFICER', 'ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable UUID customerId,
            @Valid @RequestBody CustomerRequest request) {
        LOGGER.info("OrgAPI: Update Customer...");
        CustomerResponse updated = customerService.updateCustomer(customerId, request);
        if (emailNotificationClientServicer != null) {
            emailNotificationClientServicer.sendEmailNotification(request.getEmail(), "Customer Profile Updated", "Your account information was updated");
        }
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Deactivate customer profile")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error") })
    @DeleteMapping(value = "/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public @ResponseBody ResponseEntity<Void> deactivateCustomer(@PathVariable UUID customerId) {
        LOGGER.info("OrgAPI: Deactivate Customer...");
        customerService.deactivateCustomer(customerId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all customers with pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error") })
    @GetMapping(produces = "application/json")
    @PreAuthorize("hasAnyRole('LOAN_OFFICER', 'ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody ResponseEntity<Page<CustomerResponse>> getAllCustomers(
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        LOGGER.info("OrgAPI: Get All Customers with pagination...");
        return ResponseEntity.ok(customerService.getAllCustomers(pageable));
    }

    @Operation(summary = "Get customers by KYC status with pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error") })
    @GetMapping(value = "/kyc-status/{kycStatus}", produces = "application/json")
    @PreAuthorize("hasAnyRole('LOAN_OFFICER', 'ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody ResponseEntity<Page<CustomerResponse>> getCustomersByKycStatus(
            @PathVariable KycStatus kycStatus,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        LOGGER.info("OrgAPI: Get Customers By KYC Status with pagination...");
        return ResponseEntity.ok(customerService.getCustomersByKycStatus(kycStatus, pageable));
    }

    @Operation(summary = "Search customers with pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error") })
    @GetMapping(value = "/search", produces = "application/json")
    @PreAuthorize("hasAnyRole('LOAN_OFFICER', 'ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody ResponseEntity<Page<CustomerResponse>> searchCustomers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String kycStatus,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        LOGGER.info("OrgAPI: Search Customers...");
        return ResponseEntity.ok(customerService.searchCustomers(name, email, kycStatus, pageable));
    }

    @Operation(summary = "Trigger card sync from card-service")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error") })
    @PostMapping(value = "/{customerId}/sync-cards")
    @PreAuthorize("hasAnyRole('LOAN_OFFICER', 'ADMIN')")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public @ResponseBody ResponseEntity<Void> syncCards(@PathVariable UUID customerId) {
        LOGGER.info("OrgAPI: Sync Customer Cards...");
        cardSyncClient.syncCustomerCards(customerId);
        return ResponseEntity.accepted().build();
    }
}
