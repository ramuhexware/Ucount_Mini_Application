package com.freddieapp.ratecalculator.controller;

import com.freddieapp.ratecalculator.dto.RateCalculationRequest;
import com.freddieapp.ratecalculator.dto.RateCalculationResponse;
import com.freddieapp.ratecalculator.service.RateCalculatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/rates")
public class RateCalculatorController {

    private final RateCalculatorService rateCalculatorService;

    @Autowired
    public RateCalculatorController(RateCalculatorService rateCalculatorService) {
        this.rateCalculatorService = rateCalculatorService;
    }

    @PostMapping("/calculate")
    public ResponseEntity<RateCalculationResponse> calculateRate(@RequestBody RateCalculationRequest request) {
        RateCalculationResponse response = rateCalculatorService.calculateRate(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "rate-calculator-service");
        status.put("framework", "Pure Spring Framework 6.1 (Non-Spring Boot)");
        status.put("status", "UP");
        status.put("port", 8090);
        return ResponseEntity.ok(status);
    }
}
