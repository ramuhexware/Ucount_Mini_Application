package com.freddieapp.underwriting.processor;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Rule Engine Pattern utilizing Java 17 Switch Expressions for underwriting risk decisioning.
 */
@Component
public class UnderwritingRuleProcessor {

    public enum Decision { APPROVED, REFERRED, DECLINED }
    public enum RiskLevel { LOW, MEDIUM, HIGH }
    public enum RuleCategory { PRIME_QUALIFIED, NEAR_PRIME_QUALIFIED, SUBPRIME_RISK }

    public record UnderwritingAssessmentDTO(
        Long loanId,
        BigDecimal loanAmount,
        BigDecimal propertyValue,
        BigDecimal monthlyIncome,
        BigDecimal monthlyDebt,
        Integer creditScore
    ) {}

    public record AssessmentResultDTO(
        Long loanId,
        Decision decision,
        RiskLevel riskLevel,
        BigDecimal dtiRatio,
        BigDecimal ltvRatio,
        String message
    ) {}

    public AssessmentResultDTO assessApplication(UnderwritingAssessmentDTO dto) {
        BigDecimal dti = calculateDti(dto.monthlyDebt(), dto.monthlyIncome());
        BigDecimal ltv = calculateLtv(dto.loanAmount(), dto.propertyValue());
        Decision decision = evaluateDecision(dto.creditScore(), dti, ltv);
        RiskLevel riskLevel = evaluateRiskLevel(decision, dti, ltv);

        return new AssessmentResultDTO(
            dto.loanId(),
            decision,
            riskLevel,
            dti,
            ltv,
            "Underwriting decision completed for loan: " + dto.loanId()
        );
    }

    public RuleCategory classifyRule(int creditScore) {
        if (creditScore >= 720) return RuleCategory.PRIME_QUALIFIED;
        if (creditScore >= 640) return RuleCategory.NEAR_PRIME_QUALIFIED;
        return RuleCategory.SUBPRIME_RISK;
    }

    public Decision evaluateDecision(int creditScore, BigDecimal dtiRatio, BigDecimal ltvRatio) {
        if (creditScore < 580 || dtiRatio.compareTo(new BigDecimal("50.0")) > 0 || ltvRatio.compareTo(new BigDecimal("95.0")) > 0) {
            return Decision.DECLINED;
        }

        RuleCategory category = classifyRule(creditScore);

        return switch (category) {
            case PRIME_QUALIFIED -> (dtiRatio.compareTo(new BigDecimal("43.0")) <= 0) ? Decision.APPROVED : Decision.REFERRED;
            case NEAR_PRIME_QUALIFIED -> (dtiRatio.compareTo(new BigDecimal("48.0")) <= 0) ? Decision.REFERRED : Decision.DECLINED;
            case SUBPRIME_RISK -> Decision.DECLINED;
        };
    }

    public RiskLevel evaluateRiskLevel(Decision decision, BigDecimal dtiRatio, BigDecimal ltvRatio) {
        if (decision == Decision.DECLINED) return RiskLevel.HIGH;
        if (decision == Decision.APPROVED && dtiRatio.compareTo(new BigDecimal("36.0")) <= 0 && ltvRatio.compareTo(new BigDecimal("80.0")) <= 0) {
            return RiskLevel.LOW;
        }
        return RiskLevel.MEDIUM;
    }

    public BigDecimal calculateDti(BigDecimal monthlyDebt, BigDecimal monthlyIncome) {
        if (monthlyIncome == null || monthlyIncome.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return monthlyDebt.divide(monthlyIncome, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateLtv(BigDecimal loanAmount, BigDecimal propertyValue) {
        if (propertyValue == null || propertyValue.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return loanAmount.divide(propertyValue, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
    }
}
