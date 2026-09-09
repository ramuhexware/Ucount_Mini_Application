package com.freddieapp.ratecalculator.repository;

import com.freddieapp.ratecalculator.enums.PricingTier;
import org.springframework.stereotype.Repository;

@Repository
public class RateRepository {

    public double getBaseBenchmarkRate(PricingTier tier) {
        return switch (tier) {
            case PRIME -> 5.75;
            case NEAR_PRIME -> 6.25;
            case NON_PRIME -> 7.10;
            case SUBPRIME -> 8.50;
        };
    }
}
