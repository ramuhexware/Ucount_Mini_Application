package com.freddieapp.ratecalculator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateCalculationRequest {
    private BigDecimal loanAmount;
    private BigDecimal propertyValue;
    private Integer creditScore;
    private String loanType; // e.g. PURCHASE, REFINANCE, HELOC
    private Integer loanTermMonths; // e.g. 180 (15 yrs), 360 (30 yrs)
}
