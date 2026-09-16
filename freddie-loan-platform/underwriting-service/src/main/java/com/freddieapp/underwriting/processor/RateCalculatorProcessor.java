package com.freddieapp.underwriting.processor;

import com.freddieapp.underwriting.dto.AmortizationScheduleDTO;
import com.freddieapp.underwriting.dto.AmortizationScheduleDTO.AmortizationRow;
import com.freddieapp.underwriting.dto.PricingQuoteDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Strategy Pricing & Amortization Engine for mortgage interest rate pricing and EMI generation.
 */
@Component
public class RateCalculatorProcessor {

    public PricingQuoteDTO calculatePricingQuote(Integer creditScore, BigDecimal ltvRatio) {
        BigDecimal baseRate = BigDecimal.valueOf(6.25);
        BigDecimal ltvAdj = BigDecimal.ZERO;
        String tier;

        if (creditScore >= 740) {
            tier = "PRIME";
            baseRate = BigDecimal.valueOf(6.125);
        } else if (creditScore >= 680) {
            tier = "NEAR_PRIME";
            baseRate = BigDecimal.valueOf(6.500);
        } else if (creditScore >= 620) {
            tier = "NON_PRIME";
            baseRate = BigDecimal.valueOf(7.125);
        } else {
            tier = "SUBPRIME";
            baseRate = BigDecimal.valueOf(8.250);
        }

        if (ltvRatio != null && ltvRatio.doubleValue() > 80.0) {
            ltvAdj = BigDecimal.valueOf(0.375);
        }

        BigDecimal finalRate = baseRate.add(ltvAdj);
        BigDecimal sampleLoan = BigDecimal.valueOf(350000);
        BigDecimal emi = calculateMonthlyEMI(sampleLoan, finalRate, 360);

        return new PricingQuoteDTO(tier, baseRate, ltvAdj, finalRate, emi);
    }

    public AmortizationScheduleDTO generateAmortizationSchedule(BigDecimal loanAmount, BigDecimal annualInterestRate, int termMonths) {
        BigDecimal monthlyRate = annualInterestRate.divide(BigDecimal.valueOf(1200), 8, RoundingMode.HALF_UP);
        BigDecimal emi = calculateMonthlyEMI(loanAmount, annualInterestRate, termMonths);

        List<AmortizationRow> rows = new ArrayList<>();
        BigDecimal remainingBalance = loanAmount;

        for (int month = 1; month <= termMonths; month++) {
            BigDecimal interestPayment = remainingBalance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal principalPayment = emi.subtract(interestPayment).setScale(2, RoundingMode.HALF_UP);
            remainingBalance = remainingBalance.subtract(principalPayment).setScale(2, RoundingMode.HALF_UP);

            if (month == termMonths || remainingBalance.doubleValue() < 0) {
                remainingBalance = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            }

            rows.add(new AmortizationRow(month, emi, principalPayment, interestPayment, remainingBalance));
        }

        return new AmortizationScheduleDTO(loanAmount, annualInterestRate, termMonths, emi, rows);
    }

    public BigDecimal calculateMonthlyEMI(BigDecimal principal, BigDecimal annualRatePercent, int termMonths) {
        double p = principal.doubleValue();
        double r = annualRatePercent.doubleValue() / 1200.0;
        int n = termMonths;

        if (r == 0) {
            return BigDecimal.valueOf(p / n).setScale(2, RoundingMode.HALF_UP);
        }

        double emi = (p * r * Math.pow(1 + r, n)) / (Math.pow(1 + r, n) - 1);
        return BigDecimal.valueOf(emi).setScale(2, RoundingMode.HALF_UP);
    }
}
