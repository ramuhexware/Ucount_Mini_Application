package com.freddieapp.loanorigination.processor;

import com.freddieapp.loanorigination.entity.LoanApplication;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class LoanApplicationProcessor {

    public void validateAndPrepare(LoanApplication loan) {
        if (loan.getLoanAmount() != null && loan.getPropertyValue() != null && loan.getPropertyValue().compareTo(BigDecimal.ZERO) > 0) {
            // LTV Ratio check / calculation
            BigDecimal ltv = loan.getLoanAmount().multiply(new BigDecimal("100"))
                    .divide(loan.getPropertyValue(), 2, java.math.RoundingMode.HALF_UP);
        }
    }
}
