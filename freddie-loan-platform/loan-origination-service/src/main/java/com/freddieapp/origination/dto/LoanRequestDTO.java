package com.freddieapp.origination.dto;

import java.math.BigDecimal;

public record LoanRequestDTO(
    String customerId,
    String applicantName,
    String email,
    BigDecimal loanAmount,
    BigDecimal propertyValue,
    BigDecimal monthlyIncome,
    BigDecimal monthlyDebt,
    Integer creditScore,
    Integer termMonths
) {}
