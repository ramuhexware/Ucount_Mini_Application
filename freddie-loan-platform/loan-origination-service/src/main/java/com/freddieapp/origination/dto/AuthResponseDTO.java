package com.freddieapp.origination.dto;

public record AuthResponseDTO(
    String token,
    String tokenType,
    String username,
    String role
) {}
