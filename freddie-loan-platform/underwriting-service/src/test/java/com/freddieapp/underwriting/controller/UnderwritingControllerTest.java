package com.freddieapp.underwriting.controller;

import com.freddieapp.underwriting.dto.UnderwritingRequest;
import com.freddieapp.underwriting.dto.UnderwritingResponse;
import com.freddieapp.underwriting.enums.Decision;
import com.freddieapp.underwriting.enums.RiskLevel;
import com.freddieapp.underwriting.repository.UnderwritingAssessmentRepository;
import com.freddieapp.underwriting.service.UnderwritingEngine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UnderwritingControllerTest {

    @Mock
    private UnderwritingEngine underwritingEngine;

    @Mock
    private com.freddieapp.underwriting.service.EmailNotificationClientServicer emailNotificationClientServicer;

    @Mock
    private com.freddieapp.underwriting.config.EmailNotificationClientConfig emailNotificationClientConfig;

    @InjectMocks
    private UnderwritingController underwritingController;

    private UnderwritingResponse mockResponse;

    @Before
    public void setUp() {
        mockResponse = UnderwritingResponse.builder()
                .assessmentId("UW-8801")
                .loanId("LOAN-9001")
                .customerId("CUST-500")
                .creditScore(760)
                .dtiRatio(new BigDecimal("28.00"))
                .ltvRatio(new BigDecimal("79.50"))
                .riskLevel(RiskLevel.LOW)
                .decision(Decision.APPROVED)
                .decisionReason("Low risk profile: FICO >= 680, LTV <= 80%, DTI <= 43%")
                .build();
    }

    @Test
    public void testAssessLoanSuccess() {
        when(underwritingEngine.assessLoan(any(UnderwritingRequest.class))).thenReturn(mockResponse);

        UnderwritingRequest request = UnderwritingRequest.builder()
                .loanId("LOAN-9001")
                .customerId("CUST-500")
                .annualIncome(new BigDecimal("120000.00"))
                .monthlyDebt(new BigDecimal("1800.00"))
                .loanAmount(new BigDecimal("350000.00"))
                .propertyValue(new BigDecimal("440000.00"))
                .build();

        ResponseEntity<UnderwritingResponse> response = underwritingController.assessLoan(request);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("UW-8801", response.getBody().getAssessmentId());
        assertEquals(Decision.APPROVED, response.getBody().getDecision());
    }

    @Test
    public void testOverrideDecisionSuccess() {
        when(underwritingEngine.overrideDecision("UW-8801", Decision.APPROVED, "Manual override by underwriter", "underwriter"))
                .thenReturn(mockResponse);

        ResponseEntity<UnderwritingResponse> response = underwritingController.overrideDecision(
                "UW-8801", Decision.APPROVED, "Manual override by underwriter", "underwriter");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Decision.APPROVED, response.getBody().getDecision());
    }

    @Test
    public void testGetLatestAssessmentSuccess() {
        when(underwritingEngine.getLatestAssessment("LOAN-9001")).thenReturn(mockResponse);

        ResponseEntity<UnderwritingResponse> response = underwritingController.getLatestAssessment("LOAN-9001");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("UW-8801", response.getBody().getAssessmentId());
    }
}


