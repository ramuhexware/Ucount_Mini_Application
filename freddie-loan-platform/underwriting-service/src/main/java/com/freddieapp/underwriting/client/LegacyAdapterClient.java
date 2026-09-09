package com.freddieapp.underwriting.client;

import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class LegacyAdapterClient {

    public LoanEligibilityResult checkEligibility(
            String loanId,
            String customerId,
            String customerName,
            BigDecimal loanAmount,
            BigDecimal propertyValue,
            int creditScore,
            BigDecimal annualIncome,
            BigDecimal monthlyDebt) {

        if (creditScore < 600) {
            return LoanEligibilityResult.builder()
                    .eligible(false)
                    .reason("FICO credit score below minimum pre-qualification gate (600).")
                    .bureauReference("Auto-Declined by Pre-Qual Gate")
                    .build();
        }

        return LoanEligibilityResult.builder()
                .eligible(true)
                .reason("Eligible based on internal guidelines.")
                .bureauReference("REF-" + Math.abs((loanId != null ? loanId : customerId).hashCode() % 1000000))
                .build();
    }

    public CustomerVerificationResult verifyCustomer(String customerId) {
        return CustomerVerificationResult.builder()
                .verified(true)
                .riskLevel("LOW")
                .build();
    }

    @Data
    @Builder
    public static class LoanEligibilityResult {
        private boolean eligible;
        private String reason;
        private String bureauReference;
    }

    @Data
    @Builder
    public static class CustomerVerificationResult {
        private boolean verified;
        private String riskLevel;
    }
}
