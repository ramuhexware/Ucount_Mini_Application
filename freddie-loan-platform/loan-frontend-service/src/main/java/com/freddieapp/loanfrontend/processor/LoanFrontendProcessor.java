package com.freddieapp.loanfrontend.processor;

import org.springframework.stereotype.Component;

@Component
public class LoanFrontendProcessor {

    public String sanitizeLoanId(String loanId) {
        return loanId != null ? loanId.trim().toUpperCase() : "";
    }
}
