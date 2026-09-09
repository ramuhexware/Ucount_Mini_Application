package com.freddieapp.ratecalculator.controller;

import com.freddieapp.ratecalculator.config.EmailNotificationClientConfig;
import com.freddieapp.ratecalculator.dto.RateCalculationRequest;
import com.freddieapp.ratecalculator.dto.RateCalculationResponse;
import com.freddieapp.ratecalculator.service.EmailNotificationClientServicer;
import com.freddieapp.ratecalculator.service.RateCalculatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/rates")
@Tag(name = "Rate Calculator", description = "Mortgage interest rate and payment calculation APIs")
public class RateCalculatorController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RateCalculatorController.class);

    private final RateCalculatorService rateCalculatorService;

    @Autowired
    EmailNotificationClientServicer emailNotificationClientServicer;

    @Autowired
    EmailNotificationClientConfig emailNotificationClientConfig;

    @Value("${bypassPingAuth:false}")
    private String bypassPingAuth;

    @Autowired
    public RateCalculatorController(RateCalculatorService rateCalculatorService) {
        this.rateCalculatorService = rateCalculatorService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields();
    }

    @Operation(summary = "Calculate mortgage interest rate and monthly payment estimate")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error") })
    @PostMapping(value = "/calculate", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody ResponseEntity<RateCalculationResponse> calculateRate(@RequestBody RateCalculationRequest request) {
        LOGGER.info("OrgAPI: Calculate Rate...");
        RateCalculationResponse response = rateCalculatorService.calculateRate(request);
        if (emailNotificationClientServicer != null) {
            emailNotificationClientServicer.sendEmailNotification("rate-quote@freddiemac.com", 
                    "Rate Calculation Quote Generated", "Calculated Interest Rate: " + response.getFinalInterestRate() + "%");
        }
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Service health check endpoint")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error") })
    @GetMapping(value = "/health", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody ResponseEntity<Map<String, Object>> healthCheck() {
        LOGGER.info("OrgAPI: Health Check...");
        Map<String, Object> status = new HashMap<>();
        status.put("service", "rate-calculator-service");
        status.put("framework", "Pure Spring Framework 6.1 (Non-Spring Boot)");
        status.put("status", "UP");
        status.put("port", 8090);
        return ResponseEntity.ok(status);
    }
}
