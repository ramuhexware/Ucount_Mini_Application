package com.freddieapp.ratecalculator.specification;

import com.freddieapp.ratecalculator.enums.PricingTier;

public class RateSpecification {

    public static PricingTier determineTier(int creditScore) {
        if (creditScore >= 740) return PricingTier.PRIME;
        if (creditScore >= 680) return PricingTier.NEAR_PRIME;
        if (creditScore >= 620) return PricingTier.NON_PRIME;
        return PricingTier.SUBPRIME;
    }
}
