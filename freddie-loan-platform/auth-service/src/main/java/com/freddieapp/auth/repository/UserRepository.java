package com.freddieapp.auth.repository;

import com.freddieapp.auth.enums.UserRole;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepository {

    // ─── ORM Methods ────────────────────────────────────────────────────────
    public Optional<String> findByUsername(String username) {
        if ("user".equalsIgnoreCase(username) || "admin".equalsIgnoreCase(username)) {
            return Optional.of(username);
        }
        return Optional.empty();
    }

    // ─── Native SQL Query Simulation ───────────────────────────────────────
    public Optional<UserRole> findUserRoleNative(String username) {
        if ("admin".equalsIgnoreCase(username)) {
            return Optional.of(UserRole.ADMIN);
        }
        return Optional.of(UserRole.USER);
    }
}
