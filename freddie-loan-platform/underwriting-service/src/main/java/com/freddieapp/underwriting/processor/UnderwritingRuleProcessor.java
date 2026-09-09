package com.freddieapp.underwriting.processor;

import com.freddieapp.underwriting.entity.UnderwritingAssessment;
import com.freddieapp.underwriting.enums.Decision;
import com.freddieapp.underwriting.enums.RiskLevel;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class UnderwritingRuleProcessor {

    public void evaluateRules(UnderwritingAssessment assessment) {
        if (assessment.getCreditScore() != null && assessment.getCreditScore() < 620) {
            assessment.setDecision(Decision.DECLINED);
            assessment.setRiskLevel(RiskLevel.HIGH);
            assessment.setDecisionReason("Credit score below minimum threshold of 620.");
        } else if (assessment.getDtiRatio() != null && assessment.getDtiRatio().compareTo(new BigDecimal("45.00")) > 0) {
            assessment.setDecision(Decision.MANUAL_REVIEW);
            assessment.setRiskLevel(RiskLevel.MEDIUM);
            assessment.setDecisionReason("DTI ratio exceeds 45.00%. Automated referral for manual review.");
        } else {
            assessment.setDecision(Decision.APPROVED);
            assessment.setRiskLevel(RiskLevel.LOW);
            assessment.setDecisionReason("Automated underwriting criteria satisfied.");
        }
    }
}
