package com.freddieapp.underwriting.dto;

import java.math.BigDecimal;
import java.util.List;

public record AssessmentResultDTO(
    Long loanId,
    String decision,
    String riskLevel,
    BigDecimal dtiRatio,
    BigDecimal ltvRatio,
    List<String> approvalConditions,
    List<String> riskFlags
) {}
