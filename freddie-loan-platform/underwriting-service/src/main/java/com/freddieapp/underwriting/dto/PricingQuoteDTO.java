package com.freddieapp.underwriting.dto;

import java.math.BigDecimal;

public record PricingQuoteDTO(
    String pricingTier,
    BigDecimal baseRate,
    BigDecimal ltvAdjustment,
    BigDecimal finalInterestRate,
    BigDecimal monthlyEmi
) {}
