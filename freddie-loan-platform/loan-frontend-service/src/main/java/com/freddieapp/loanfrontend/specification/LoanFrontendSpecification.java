package com.freddieapp.loanfrontend.specification;

import com.freddieapp.loanfrontend.enums.FrontendLoanStatus;

public class LoanFrontendSpecification {

    public static boolean isSubmitted(FrontendLoanStatus status) {
        return status == FrontendLoanStatus.SUBMITTED;
    }
}
