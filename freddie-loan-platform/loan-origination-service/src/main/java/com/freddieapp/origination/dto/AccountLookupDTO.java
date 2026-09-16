package com.freddieapp.origination.dto;

import java.util.List;
import java.util.Map;

public record AccountLookupDTO(
    List<String> accountTypes,
    List<String> roles,
    Map<String, String> metadata
) {}
