package com.freddieapp.origination.dto;

import java.time.LocalDateTime;

public record Stage1UserResponseDTO(
    String userId,
    String orgName,
    String email,
    Stage1Status status,
    LocalDateTime createdAt
) {}
