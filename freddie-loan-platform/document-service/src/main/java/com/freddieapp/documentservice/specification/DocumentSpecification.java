package com.freddieapp.documentservice.specification;

import com.freddieapp.documentservice.enums.DocumentStatus;

public class DocumentSpecification {

    public static boolean matches(String loanId, String status, String targetLoanId, DocumentStatus targetStatus) {
        boolean loanMatches = targetLoanId == null || targetLoanId.equalsIgnoreCase(loanId);
        boolean statusMatches = targetStatus == null || targetStatus.name().equalsIgnoreCase(status);
        return loanMatches && statusMatches;
    }
}
