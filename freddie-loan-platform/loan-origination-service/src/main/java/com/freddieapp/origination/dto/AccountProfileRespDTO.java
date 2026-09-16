package com.freddieapp.origination.dto;

import java.util.List;

public record AccountProfileRespDTO(
    String idCntprtyAcct,
    String verificationStatus,
    List<String> assignedPermissions
) {}
