package com.freddieapp.auth.specification;

import com.freddieapp.auth.enums.UserRole;

public class UserSpecification {

    public static boolean matchesRole(UserRole targetRole, UserRole userRole) {
        return targetRole == null || targetRole == userRole;
    }
}
