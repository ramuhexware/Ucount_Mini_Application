package com.freddieapp.ratecalculator.service;

import com.freddieapp.ratecalculator.client.notification.NotificationClient;
import com.freddieapp.ratecalculator.dto.RateCalculationRequest;
import com.freddieapp.ratecalculator.dto.RateCalculationResponse;
import com.freddieapp.ratecalculator.processor.RateCalculationProcessor;
import com.freddieapp.ratecalculator.repository.RateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class RateCalculatorService {

    private final RateRepository rateRepository;
    private final RateCalculationProcessor processor;
    private final NotificationClient notificationClient;

    @Autowired
    public RateCalculatorService(RateRepository rateRepository, RateCalculationProcessor processor, NotificationClient notificationClient) {
        this.rateRepository = rateRepository;
        this.processor = processor;
        this.notificationClient = notificationClient;
    }

    public RateCalculationResponse calculateRate(RateCalculationRequest request) {
        BigDecimal loanAmount = request.getLoanAmount() != null ? request.getLoanAmount() : new BigDecimal("300000.00");
        BigDecimal propertyValue = request.getPropertyValue() != null ? request.getPropertyValue() : new BigDecimal("400000.00");
        int creditScore = request.getCreditScore() != null ? request.getCreditScore() : 720;
        int termMonths = request.getLoanTermMonths() != null ? request.getLoanTermMonths() : 360;

        // Base Benchmark Interest Rate (e.g. 6.25%)
        BigDecimal baseRate = new BigDecimal("6.25");

        // Credit Score Adjustment
        BigDecimal creditAdj;
        String tier;
        if (creditScore >= 760) {
            creditAdj = new BigDecimal("-0.375");
            tier = "PRIME_EXCELLENT";
        } else if (creditScore >= 700) {
            creditAdj = BigDecimal.ZERO;
            tier = "PRIME_GOOD";
        } else if (creditScore >= 660) {
            creditAdj = new BigDecimal("0.500");
            tier = "STANDARD";
        } else {
            creditAdj = new BigDecimal("1.250");
            tier = "SUBPRIME_HIGH_RISK";
        }

        // LTV Calculation
        BigDecimal ltvRatio = loanAmount.multiply(new BigDecimal("100"))
                .divide(propertyValue, 2, RoundingMode.HALF_UP);

        BigDecimal ltvAdj = BigDecimal.ZERO;
        if (ltvRatio.compareTo(new BigDecimal("90.00")) > 0) {
            ltvAdj = new BigDecimal("0.375");
        } else if (ltvRatio.compareTo(new BigDecimal("80.00")) > 0) {
            ltvAdj = new BigDecimal("0.250");
        }

        BigDecimal finalRate = baseRate.add(creditAdj).add(ltvAdj);

        // EMI Calculation formula: P * r * (1+r)^n / ((1+r)^n - 1)
        double annualRateDouble = finalRate.doubleValue() / 100.0;
        double monthlyRate = annualRateDouble / 12.0;
        double principal = loanAmount.doubleValue();

        double emiDouble;
        if (monthlyRate > 0) {
            emiDouble = (principal * monthlyRate * Math.pow(1 + monthlyRate, termMonths)) /
                    (Math.pow(1 + monthlyRate, termMonths) - 1);
        } else {
            emiDouble = principal / termMonths;
        }

        BigDecimal monthlyEmi = BigDecimal.valueOf(emiDouble).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalPayment = monthlyEmi.multiply(new BigDecimal(termMonths)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalInterest = totalPayment.subtract(loanAmount).setScale(2, RoundingMode.HALF_UP);

        return RateCalculationResponse.builder()
                .baseRate(baseRate)
                .creditScoreAdjustment(creditAdj)
                .ltvAdjustment(ltvAdj)
                .finalInterestRate(finalRate)
                .monthlyPaymentEmi(monthlyEmi)
                .totalPayment(totalPayment)
                .totalInterestPaid(totalInterest)
                .ltvRatio(ltvRatio)
                .pricingTier(tier)
                .calculationEngine("Freddie Mac Pure Spring Pricing Engine v1.0")
                .calculatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
    }
}
