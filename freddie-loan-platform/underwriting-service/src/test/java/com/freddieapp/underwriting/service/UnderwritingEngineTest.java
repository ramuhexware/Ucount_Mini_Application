package com.freddieapp.underwriting.service;

import com.freddieapp.underwriting.client.LegacyAdapterClient;
import com.freddieapp.underwriting.client.LegacyAdapterClient.CustomerVerificationResult;
import com.freddieapp.underwriting.client.LegacyAdapterClient.LoanEligibilityResult;
import com.freddieapp.underwriting.dto.UnderwritingRequest;
import com.freddieapp.underwriting.dto.UnderwritingResponse;
import com.freddieapp.underwriting.entity.UnderwritingAssessment;
import com.freddieapp.underwriting.enums.Decision;
import com.freddieapp.underwriting.enums.RiskLevel;
import com.freddieapp.underwriting.repository.UnderwritingAssessmentRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * JUnit 4 unit tests for UnderwritingEngine.
 */
@RunWith(MockitoJUnitRunner.class)
public class UnderwritingEngineTest {

    @Mock
    private UnderwritingAssessmentRepository assessmentRepository;

    @Mock
    private LegacyAdapterClient legacyAdapterClient;

    @InjectMocks
    private UnderwritingEngine underwritingEngine;

    private UnderwritingRequest goodCreditRequest;
    private UnderwritingRequest poorCreditRequest;
    private UnderwritingRequest highDtiRequest;
    private CustomerVerificationResult lowRiskVerification;
    private CustomerVerificationResult highRiskVerification;
    private LoanEligibilityResult eligibleResult;
    private LoanEligibilityResult ineligibleResult;
    private UnderwritingAssessment mockAssessment;
    private String loanId;
    private String customerId;

    @Before
    public void setUp() {
        loanId = UUID.randomUUID().toString();
        customerId = UUID.randomUUID().toString();

        // Good credit: 750 score, low DTI, low LTV
        goodCreditRequest = UnderwritingRequest.builder()
                .loanId(loanId)
                .customerId(customerId)
                .loanAmount(new BigDecimal("300000.00"))
                .propertyValue(new BigDecimal("500000.00"))      // LTV = 60%
                .annualIncome(new BigDecimal("120000.00"))
                .monthlyDebt(new BigDecimal("1500.00"))           // DTI = 15%
                .build();

        // Poor credit scenario (high LTV, high DTI)
        poorCreditRequest = UnderwritingRequest.builder()
                .loanId(loanId)
                .customerId(customerId)
                .loanAmount(new BigDecimal("480000.00"))
                .propertyValue(new BigDecimal("490000.00"))       // LTV = ~98% -> CRITICAL
                .annualIncome(new BigDecimal("60000.00"))
                .monthlyDebt(new BigDecimal("3500.00"))           // DTI = 70% -> CRITICAL
                .build();

        // High DTI scenario
        highDtiRequest = UnderwritingRequest.builder()
                .loanId(loanId)
                .customerId(customerId)
                .loanAmount(new BigDecimal("350000.00"))
                .propertyValue(new BigDecimal("450000.00"))       // LTV = 77.8%
                .annualIncome(new BigDecimal("90000.00"))
                .monthlyDebt(new BigDecimal("4000.00"))           // DTI = 53.3% -> HIGH
                .build();

        lowRiskVerification = CustomerVerificationResult.builder()
                .verified(true)
                .riskLevel("LOW")
                .build();

        highRiskVerification = CustomerVerificationResult.builder()
                .verified(true)
                .riskLevel("HIGH")
                .build();

        eligibleResult = LoanEligibilityResult.builder()
                .eligible(true)
                .reason("Meets all guidelines")
                .bureauReference("BUREAU-REF-001")
                .build();

        ineligibleResult = LoanEligibilityResult.builder()
                .eligible(false)
                .reason("Credit score too low")
                .bureauReference("")
                .build();

        mockAssessment = UnderwritingAssessment.builder()
                .assessmentId(UUID.randomUUID().toString())
                .loanId(loanId)
                .customerId(customerId)
                .creditScore(740)
                .dtiRatio(new BigDecimal("15.00"))
                .ltvRatio(new BigDecimal("60.00"))
                .riskLevel(RiskLevel.LOW)
                .decision(Decision.APPROVED)
                .decisionReason("System Approved: Criteria meets Freddie Mac lending guidelines.")
                .assessedBy("AUTOMATED_ENGINE")
                .bureauReference("BUREAU-REF-001")
                .build();
    }

    // ─── assessLoan — Happy Path ──────────────────────────────────────────────

    @Test
    public void assessLoan_goodCredit_approved() {
        when(legacyAdapterClient.verifyCustomer(customerId)).thenReturn(lowRiskVerification);
        when(legacyAdapterClient.checkEligibility(
                anyString(), anyString(), anyString(),
                any(BigDecimal.class), any(BigDecimal.class),
                anyInt(), any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(eligibleResult);
        when(assessmentRepository.save(any(UnderwritingAssessment.class))).thenReturn(mockAssessment);

        UnderwritingResponse response = underwritingEngine.assessLoan(goodCreditRequest);

        assertNotNull(response);
        assertEquals(Decision.APPROVED, response.getDecision());
        assertEquals(loanId, response.getLoanId());
        assertNotNull(response.getAmortizationSchedule());
        assertFalse(response.getAmortizationSchedule().isEmpty());
    }

    @Test
    public void assessLoan_amortizationScheduleHas360Payments() {
        when(legacyAdapterClient.verifyCustomer(customerId)).thenReturn(lowRiskVerification);
        when(legacyAdapterClient.checkEligibility(
                anyString(), anyString(), anyString(),
                any(BigDecimal.class), any(BigDecimal.class),
                anyInt(), any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(eligibleResult);
        when(assessmentRepository.save(any(UnderwritingAssessment.class))).thenReturn(mockAssessment);

        UnderwritingResponse response = underwritingEngine.assessLoan(goodCreditRequest);

        assertNotNull(response.getAmortizationSchedule());
        assertEquals(360, response.getAmortizationSchedule().size());
    }

    @Test
    public void assessLoan_llpaAndPmiSet() {
        when(legacyAdapterClient.verifyCustomer(customerId)).thenReturn(lowRiskVerification);
        when(legacyAdapterClient.checkEligibility(
                anyString(), anyString(), anyString(),
                any(BigDecimal.class), any(BigDecimal.class),
                anyInt(), any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(eligibleResult);
        when(assessmentRepository.save(any(UnderwritingAssessment.class))).thenReturn(mockAssessment);

        UnderwritingResponse response = underwritingEngine.assessLoan(goodCreditRequest);

        assertNotNull(response.getLlpaSurcharge());
        assertNotNull(response.getPmiMonthlyPremium());
        assertNotNull(response.getBaseInterestRate());
        assertNotNull(response.getAdjustedInterestRate());
        assertTrue(response.getAdjustedInterestRate().compareTo(response.getBaseInterestRate()) >= 0);
    }

    // ─── assessLoan — Declined Paths ─────────────────────────────────────────

    @Test
    public void assessLoan_ineligible_declined() {
        UnderwritingAssessment declinedAssessment = UnderwritingAssessment.builder()
                .assessmentId(UUID.randomUUID().toString())
                .loanId(loanId)
                .customerId(customerId)
                .creditScore(580)
                .riskLevel(RiskLevel.CRITICAL)
                .decision(Decision.DECLINED)
                .decisionReason("System Declined: Credit score too low")
                .assessedBy("AUTOMATED_ENGINE")
                .build();

        when(legacyAdapterClient.verifyCustomer(customerId)).thenReturn(lowRiskVerification);
        when(legacyAdapterClient.checkEligibility(
                anyString(), anyString(), anyString(),
                any(BigDecimal.class), any(BigDecimal.class),
                anyInt(), any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(ineligibleResult);
        when(assessmentRepository.save(any(UnderwritingAssessment.class))).thenReturn(declinedAssessment);

        UnderwritingResponse response = underwritingEngine.assessLoan(poorCreditRequest);

        assertNotNull(response);
        assertEquals(Decision.DECLINED, response.getDecision());
    }

    @Test
    public void assessLoan_highVerificationRisk_referred() {
        UnderwritingAssessment referredAssessment = UnderwritingAssessment.builder()
                .assessmentId(UUID.randomUUID().toString())
                .loanId(loanId)
                .customerId(customerId)
                .creditScore(700)
                .riskLevel(RiskLevel.MEDIUM)
                .decision(Decision.REFERRED)
                .decisionReason("System Referred: Requires manual underwriting review")
                .assessedBy("AUTOMATED_ENGINE")
                .build();

        when(legacyAdapterClient.verifyCustomer(customerId)).thenReturn(highRiskVerification);
        when(legacyAdapterClient.checkEligibility(
                anyString(), anyString(), anyString(),
                any(BigDecimal.class), any(BigDecimal.class),
                anyInt(), any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(eligibleResult);
        when(assessmentRepository.save(any(UnderwritingAssessment.class))).thenReturn(referredAssessment);

        UnderwritingResponse response = underwritingEngine.assessLoan(goodCreditRequest);

        assertNotNull(response);
        assertEquals(Decision.REFERRED, response.getDecision());
    }

    // ─── overrideDecision ────────────────────────────────────────────────────

    @Test
    public void overrideDecision_success() {
        String assessmentId = UUID.randomUUID().toString();
        UnderwritingAssessment existing = UnderwritingAssessment.builder()
                .assessmentId(assessmentId)
                .loanId(loanId)
                .customerId(customerId)
                .riskLevel(RiskLevel.HIGH)
                .decision(Decision.REFERRED)
                .build();

        UnderwritingAssessment overridden = UnderwritingAssessment.builder()
                .assessmentId(assessmentId)
                .loanId(loanId)
                .customerId(customerId)
                .riskLevel(RiskLevel.HIGH)
                .decision(Decision.APPROVED)
                .decisionReason("Override by jsmith: Manual approval after income verification")
                .assessedBy("jsmith")
                .build();

        when(assessmentRepository.findById(assessmentId))
                .thenReturn(Optional.of(existing))
                .thenReturn(Optional.of(overridden));
        when(assessmentRepository.recordDecisionNative(
                anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(1);

        UnderwritingResponse response = underwritingEngine.overrideDecision(
                assessmentId, Decision.APPROVED, "Manual approval after income verification", "jsmith");

        assertNotNull(response);
        assertEquals(Decision.APPROVED, response.getDecision());
    }

    @Test(expected = IllegalArgumentException.class)
    public void overrideDecision_assessmentNotFound_throwsException() {
        when(assessmentRepository.findById(anyString())).thenReturn(Optional.empty());

        underwritingEngine.overrideDecision("bad-id", Decision.APPROVED, "test", "underwriter");
    }

    // ─── DTI / LTV boundary tests ─────────────────────────────────────────────

    @Test
    public void assessLoan_zeroDti_handledGracefully() {
        UnderwritingRequest zeroDebtRequest = UnderwritingRequest.builder()
                .loanId(loanId)
                .customerId(customerId)
                .loanAmount(new BigDecimal("200000.00"))
                .propertyValue(new BigDecimal("400000.00"))
                .annualIncome(new BigDecimal("100000.00"))
                .monthlyDebt(BigDecimal.ZERO)
                .build();

        when(legacyAdapterClient.verifyCustomer(customerId)).thenReturn(lowRiskVerification);
        when(legacyAdapterClient.checkEligibility(
                anyString(), anyString(), anyString(),
                any(BigDecimal.class), any(BigDecimal.class),
                anyInt(), any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(eligibleResult);
        when(assessmentRepository.save(any(UnderwritingAssessment.class))).thenReturn(mockAssessment);

        UnderwritingResponse response = underwritingEngine.assessLoan(zeroDebtRequest);

        assertNotNull(response);
    }
}
