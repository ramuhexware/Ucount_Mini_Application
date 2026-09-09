package com.freddieapp.ratecalculator.controller;

import com.freddieapp.ratecalculator.dto.RateCalculationRequest;
import com.freddieapp.ratecalculator.dto.RateCalculationResponse;
import com.freddieapp.ratecalculator.service.RateCalculatorService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RateCalculatorControllerTest {

    @Mock
    private RateCalculatorService rateCalculatorService;

    @Mock
    private com.freddieapp.ratecalculator.service.EmailNotificationClientServicer emailNotificationClientServicer;

    @Mock
    private com.freddieapp.ratecalculator.config.EmailNotificationClientConfig emailNotificationClientConfig;

    @InjectMocks
    private RateCalculatorController rateCalculatorController;

    private RateCalculationResponse mockResponse;

    @Before
    public void setUp() {
        mockResponse = RateCalculationResponse.builder()
                .baseRate(new BigDecimal("6.25"))
                .creditScoreAdjustment(BigDecimal.ZERO)
                .ltvAdjustment(BigDecimal.ZERO)
                .finalInterestRate(new BigDecimal("6.25"))
                .monthlyPaymentEmi(new BigDecimal("1847.15"))
                .totalPayment(new BigDecimal("664974.00"))
                .totalInterestPaid(new BigDecimal("364974.00"))
                .pricingTier("PRIME_GOOD")
                .build();
    }

    @Test
    public void testCalculateRateSuccess() {
        when(rateCalculatorService.calculateRate(any(RateCalculationRequest.class))).thenReturn(mockResponse);

        RateCalculationRequest request = RateCalculationRequest.builder()
                .loanAmount(new BigDecimal("300000.00"))
                .propertyValue(new BigDecimal("400000.00"))
                .creditScore(720)
                .loanTermMonths(360)
                .build();

        ResponseEntity<RateCalculationResponse> response = rateCalculatorController.calculateRate(request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(new BigDecimal("6.25"), response.getBody().getFinalInterestRate());
        assertEquals("PRIME_GOOD", response.getBody().getPricingTier());
    }

    @Test
    public void testHealthCheckSuccess() {
        ResponseEntity<Map<String, Object>> response = rateCalculatorController.healthCheck();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("rate-calculator-service", response.getBody().get("service"));
        assertEquals("UP", response.getBody().get("status"));
        assertEquals(8090, response.getBody().get("port"));
    }
}
