package com.freddieapp.ratecalculator.service;

import com.freddieapp.ratecalculator.dto.RateCalculationRequest;
import com.freddieapp.ratecalculator.dto.RateCalculationResponse;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class RateCalculatorServiceTest {

    private RateCalculatorService rateCalculatorService;

    @Before
    public void setUp() {
        rateCalculatorService = new RateCalculatorService(new com.freddieapp.ratecalculator.repository.RateRepository(), new com.freddieapp.ratecalculator.processor.RateCalculationProcessor(), new com.freddieapp.ratecalculator.client.notification.NotificationClient());
    }

    @Test
    public void testPrimeCreditScoreCalculation() {
        RateCalculationRequest request = RateCalculationRequest.builder()
                .loanAmount(new BigDecimal("300000.00"))
                .propertyValue(new BigDecimal("400000.00")) // 75% LTV
                .creditScore(780) // Excellent prime -> -0.375%
                .loanTermMonths(360)
                .build();

        RateCalculationResponse response = rateCalculatorService.calculateRate(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("6.25"), response.getBaseRate());
        assertEquals(new BigDecimal("-0.375"), response.getCreditScoreAdjustment());
        assertEquals(BigDecimal.ZERO.setScale(0), response.getLtvAdjustment());
        assertEquals(new BigDecimal("5.875"), response.getFinalInterestRate());
        assertEquals("PRIME_EXCELLENT", response.getPricingTier());
        assertTrue(response.getMonthlyPaymentEmi().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    public void testSubprimeCreditScoreWithHighLtv() {
        RateCalculationRequest request = RateCalculationRequest.builder()
                .loanAmount(new BigDecimal("380000.00"))
                .propertyValue(new BigDecimal("400000.00")) // 95% LTV -> +0.375%
                .creditScore(640) // Subprime -> +1.250%
                .loanTermMonths(360)
                .build();

        RateCalculationResponse response = rateCalculatorService.calculateRate(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("6.25"), response.getBaseRate());
        assertEquals(new BigDecimal("1.250"), response.getCreditScoreAdjustment());
        assertEquals(new BigDecimal("0.375"), response.getLtvAdjustment());
        assertEquals(new BigDecimal("7.875"), response.getFinalInterestRate());
        assertEquals("SUBPRIME_HIGH_RISK", response.getPricingTier());
    }
}
