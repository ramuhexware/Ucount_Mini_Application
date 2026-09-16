package com.freddieapp.underwriting.dto;

import java.util.List;

public record OrganizationDTO(
    String orgId,
    String orgName,
    String status,
    List<String> activeServices
) {}
