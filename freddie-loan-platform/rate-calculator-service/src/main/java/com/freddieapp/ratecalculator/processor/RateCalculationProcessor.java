package com.freddieapp.ratecalculator.processor;

import org.springframework.stereotype.Component;

@Component
public class RateCalculationProcessor {

    public double computeBaseRate(int creditScore, double ltv) {
        double baseRate = 6.50;
        if (creditScore >= 740) {
            baseRate -= 0.50;
        } else if (creditScore < 680) {
            baseRate += 0.75;
        }
        if (ltv > 80.0) {
            baseRate += 0.25;
        }
        return baseRate;
    }
}
