package com.freddieapp.ratecalculator.repository;

import com.freddieapp.ratecalculator.enums.PricingTier;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public class RateRepository {

    // ─── ORM Method ──────────────────────────────────────────────────────────
    public BigDecimal fetchBaseRateByCreditTier(int creditScore) {
        if (creditScore >= 760) {
            return new BigDecimal("5.875"); // 6.25 - 0.375
        } else if (creditScore >= 700) {
            return new BigDecimal("6.250"); // Base 6.25
        } else if (creditScore >= 660) {
            return new BigDecimal("6.750"); // 6.25 + 0.50
        } else {
            return new BigDecimal("7.500"); // 6.25 + 1.25
        }
    }

    public PricingTier determinePricingTier(int creditScore) {
        if (creditScore >= 760) {
            return PricingTier.PRIME;
        } else if (creditScore >= 700) {
            return PricingTier.NEAR_PRIME;
        } else if (creditScore >= 660) {
            return PricingTier.NON_PRIME;
        } else {
            return PricingTier.SUBPRIME;
        }
    }

    public double getBaseBenchmarkRate(PricingTier tier) {
        return switch (tier) {
            case PRIME -> 5.75;
            case NEAR_PRIME -> 6.25;
            case NON_PRIME -> 7.10;
            case SUBPRIME -> 8.50;
        };
    }

    // ─── Native SQL Query Simulation ─────────────────────────────────────────
    public BigDecimal calculateBaseRateNative(int creditScore, double ltvRatio) {
        BigDecimal baseRate = fetchBaseRateByCreditTier(creditScore);
        BigDecimal ltvAdj = BigDecimal.ZERO;
        if (ltvRatio > 90.00) {
            ltvAdj = new BigDecimal("0.375");
        } else if (ltvRatio > 80.00) {
            ltvAdj = new BigDecimal("0.250");
        }
        return baseRate.add(ltvAdj);
    }
}
