package com.freddieapp.underwriting.dto;

import java.math.BigDecimal;
import java.util.List;

public record AmortizationScheduleDTO(
    BigDecimal loanAmount,
    BigDecimal interestRate,
    int termMonths,
    BigDecimal monthlyPayment,
    List<AmortizationRow> schedule
) {
    public record AmortizationRow(
        int month,
        BigDecimal payment,
        BigDecimal principal,
        BigDecimal interest,
        BigDecimal remainingBalance
    ) {}
}
