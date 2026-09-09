package com.freddieapp.underwriting.specification;

import com.freddieapp.underwriting.entity.UnderwritingAssessment;
import com.freddieapp.underwriting.enums.Decision;
import com.freddieapp.underwriting.enums.RiskLevel;
import org.springframework.data.jpa.domain.Specification;

public class UnderwritingSpecification {

    public static Specification<UnderwritingAssessment> hasLoanId(String loanId) {
        return (root, query, cb) -> loanId == null ? null : cb.equal(root.get("loanId"), loanId);
    }

    public static Specification<UnderwritingAssessment> hasDecision(Decision decision) {
        return (root, query, cb) -> decision == null ? null : cb.equal(root.get("decision"), decision);
    }

    public static Specification<UnderwritingAssessment> hasRiskLevel(RiskLevel riskLevel) {
        return (root, query, cb) -> riskLevel == null ? null : cb.equal(root.get("riskLevel"), riskLevel);
    }
}
