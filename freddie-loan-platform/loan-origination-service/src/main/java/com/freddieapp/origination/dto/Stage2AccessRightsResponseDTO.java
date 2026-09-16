package com.freddieapp.origination.dto;

import java.util.List;

public record Stage2AccessRightsResponseDTO(
    String userId,
    UserType userType,
    List<String> accessRights
) {}
