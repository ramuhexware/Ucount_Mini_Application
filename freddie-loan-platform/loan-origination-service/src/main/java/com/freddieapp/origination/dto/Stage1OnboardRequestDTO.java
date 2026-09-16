package com.freddieapp.origination.dto;

public record Stage1OnboardRequestDTO(
    String orgName,
    String taxId,
    String email,
    String phone,
    String address,
    String networkDomain
) {}
