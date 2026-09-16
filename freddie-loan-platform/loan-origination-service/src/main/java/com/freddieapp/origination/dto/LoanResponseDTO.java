package com.freddieapp.origination.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LoanResponseDTO(
    Long id,
    String customerId,
    String applicantName,
    String email,
    BigDecimal loanAmount,
    BigDecimal propertyValue,
    Integer creditScore,
    String status,
    LocalDateTime createdAt
) {}
