package com.freddieapp.loanorigination.specification;

import com.freddieapp.loanorigination.entity.LoanApplication;
import com.freddieapp.loanorigination.enums.LoanStatus;
import com.freddieapp.loanorigination.enums.LoanType;
import org.springframework.data.jpa.domain.Specification;

public class LoanSpecification {

    public static Specification<LoanApplication> hasCustomerId(String customerId) {
        return (root, query, cb) -> customerId == null ? null : cb.equal(root.get("customerId"), customerId);
    }

    public static Specification<LoanApplication> hasLoanStatus(LoanStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("loanStatus"), status);
    }

    public static Specification<LoanApplication> hasLoanType(LoanType loanType) {
        return (root, query, cb) -> loanType == null ? null : cb.equal(root.get("loanType"), loanType);
    }
}
