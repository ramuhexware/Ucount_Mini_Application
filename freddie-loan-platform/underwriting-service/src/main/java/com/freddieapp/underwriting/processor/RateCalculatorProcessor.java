package com.freddieapp.underwriting.processor;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Strategy Pattern Math Engine for interest rate calculation, pricing tiers, and amortization schedules.
 */
@Component
public class RateCalculatorProcessor {

    public enum PricingTier { PRIME, NEAR_PRIME, NON_PRIME, SUBPRIME }

    public record AmortizationPayment(int month, BigDecimal principalPayment, BigDecimal interestPayment, BigDecimal remainingBalance) {}

    private static final BigDecimal BASE_BENCHMARK_RATE = new BigDecimal("6.25");

    public PricingTier determinePricingTier(int creditScore) {
        if (creditScore >= 740) return PricingTier.PRIME;
        if (creditScore >= 680) return PricingTier.NEAR_PRIME;
        if (creditScore >= 620) return PricingTier.NON_PRIME;
        return PricingTier.SUBPRIME;
    }

    public BigDecimal calculateAdjustedRate(int creditScore, BigDecimal ltvRatio) {
        PricingTier tier = determinePricingTier(creditScore);
        BigDecimal tierAdjustment = switch (tier) {
            case PRIME -> new BigDecimal("-0.25");
            case NEAR_PRIME -> BigDecimal.ZERO;
            case NON_PRIME -> new BigDecimal("0.75");
            case SUBPRIME -> new BigDecimal("2.50");
        };

        BigDecimal ltvSurcharge = (ltvRatio != null && ltvRatio.compareTo(new BigDecimal("80.0")) > 0)
            ? new BigDecimal("0.50")
            : BigDecimal.ZERO;

        return BASE_BENCHMARK_RATE.add(tierAdjustment).add(ltvSurcharge).setScale(3, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateMonthlyEmi(BigDecimal principal, BigDecimal annualRate, int termMonths) {
        if (principal == null || principal.compareTo(BigDecimal.ZERO) <= 0 || termMonths <= 0) {
            return BigDecimal.ZERO;
        }
        double p = principal.doubleValue();
        double r = annualRate.doubleValue() / 100 / 12;
        int n = termMonths;

        double emi = (p * r * Math.pow(1 + r, n)) / (Math.pow(1 + r, n) - 1);
        return BigDecimal.valueOf(emi).setScale(2, RoundingMode.HALF_UP);
    }

    public List<AmortizationPayment> generateAmortizationSchedule(BigDecimal principal, BigDecimal annualRate, int termMonths) {
        List<AmortizationPayment> schedule = new ArrayList<>();
        BigDecimal monthlyEmi = calculateMonthlyEmi(principal, annualRate, termMonths);
        BigDecimal remainingBalance = principal;
        BigDecimal monthlyRate = annualRate.divide(new BigDecimal("1200"), 8, RoundingMode.HALF_UP);

        for (int month = 1; month <= termMonths; month++) {
            BigDecimal interestPayment = remainingBalance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal principalPayment = monthlyEmi.subtract(interestPayment).setScale(2, RoundingMode.HALF_UP);
            remainingBalance = remainingBalance.subtract(principalPayment).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

            schedule.add(new AmortizationPayment(month, principalPayment, interestPayment, remainingBalance));
        }
        return schedule;
    }
}
