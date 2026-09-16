package com.freddieapp.origination.dto;

import java.math.BigDecimal;

public record Stage2ProfileRequestDTO(
    String userId,
    UserType userType,
    String ratingHistory,
    BigDecimal annualRevenue,
    int yearsInBusiness
) {}
