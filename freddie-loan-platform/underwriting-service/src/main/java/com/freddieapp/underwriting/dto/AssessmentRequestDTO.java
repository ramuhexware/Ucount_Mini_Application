package com.freddieapp.underwriting.dto;

import java.math.BigDecimal;

public record AssessmentRequestDTO(
    Long loanId,
    String customerId,
    BigDecimal loanAmount,
    BigDecimal propertyValue,
    BigDecimal monthlyIncome,
    BigDecimal monthlyDebt,
    Integer creditScore,
    Integer termMonths
) {}
