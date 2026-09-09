package com.freddieapp.ratecalculator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateCalculationResponse {
    private BigDecimal baseRate;
    private BigDecimal creditScoreAdjustment;
    private BigDecimal ltvAdjustment;
    private BigDecimal finalInterestRate;
    private BigDecimal monthlyPaymentEmi;
    private BigDecimal totalPayment;
    private BigDecimal totalInterestPaid;
    private BigDecimal ltvRatio;
    private String pricingTier;
    private String calculationEngine;
    private String calculatedAt;
}
