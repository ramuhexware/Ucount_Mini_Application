package com.freddieapp.underwriting.processor;

import com.freddieapp.underwriting.dto.AssessmentRequestDTO;
import com.freddieapp.underwriting.dto.AssessmentResultDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Strategy pattern risk decision engine using Java 17 Switch Expressions.
 */
@Component
public class UnderwritingRuleProcessor {

    public AssessmentResultDTO evaluateRisk(AssessmentRequestDTO request) {
        BigDecimal monthlyIncome = request.monthlyIncome();
        BigDecimal monthlyDebt = request.monthlyDebt();
        BigDecimal loanAmount = request.loanAmount();
        BigDecimal propertyValue = request.propertyValue();
        int creditScore = request.creditScore() != null ? request.creditScore() : 650;

        // DTI Ratio = (Monthly Debt / Monthly Income) * 100
        BigDecimal dti = monthlyDebt.divide(monthlyIncome, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        
        // LTV Ratio = (Loan Amount / Property Value) * 100
        BigDecimal ltv = loanAmount.divide(propertyValue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

        List<String> conditions = new ArrayList<>();
        List<String> riskFlags = new ArrayList<>();

        String decision;
        String riskLevel;

        if (creditScore >= 740 && dti.doubleValue() <= 36.0 && ltv.doubleValue() <= 80.0) {
            decision = "APPROVED";
            riskLevel = "LOW";
            conditions.add("Automatic Approval - Tier 1 Prime Credit");
        } else if (creditScore >= 660 && dti.doubleValue() <= 43.0 && ltv.doubleValue() <= 90.0) {
            decision = "APPROVED";
            riskLevel = "MEDIUM";
            conditions.add("Standard Approval - Verification of W2 and Appraisal required");
            if (ltv.doubleValue() > 80.0) {
                riskFlags.add("Private Mortgage Insurance (PMI) Required - LTV > 80%");
            }
        } else if (creditScore >= 620 && dti.doubleValue() <= 50.0) {
            decision = "REFERRED";
            riskLevel = "HIGH";
            riskFlags.add("Manual Underwriting Referral - Elevated DTI or Credit Risk");
            conditions.add("Manual Underwriter Review & DeskTop Underwriter Audit Required");
        } else {
            decision = "DECLINED";
            riskLevel = "CRITICAL";
            riskFlags.add("Excessive Credit Risk - Credit score below minimum 620 or DTI > 50%");
        }

        return new AssessmentResultDTO(
            request.loanId(),
            decision,
            riskLevel,
            dti.setScale(2, RoundingMode.HALF_UP),
            ltv.setScale(2, RoundingMode.HALF_UP),
            conditions,
            riskFlags
        );
    }
}
