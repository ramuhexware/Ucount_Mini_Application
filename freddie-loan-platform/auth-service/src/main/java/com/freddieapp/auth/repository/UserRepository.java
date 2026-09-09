package com.freddieapp.auth.repository;

import com.freddieapp.auth.dto.UserDto;
import com.freddieapp.auth.enums.UserRole;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class UserRepository {

    private final Map<String, UserDto> userStore = new ConcurrentHashMap<>();
    private final Map<String, String> passwordStore = new ConcurrentHashMap<>();

    public UserRepository() {
        initUsers();
    }

    private void initUsers() {
        addUser("USR-001", "admin", "admin123", "System Administrator", "admin@freddiemac.com",
                List.of("ADMIN", "LOAN_OFFICER", "UNDERWRITER", "CUSTOMER"));

        addUser("USR-002", "officer", "officer123", "Sarah Jenkins (Loan Officer)", "officer@freddiemac.com",
                List.of("LOAN_OFFICER", "CUSTOMER"));

        addUser("USR-003", "underwriter", "underwriter123", "Michael Vance (Senior Underwriter)", "underwriter@freddiemac.com",
                List.of("UNDERWRITER", "CUSTOMER"));

        addUser("USR-004", "customer", "customer123", "John Doe (Borrower)", "john.doe@example.com",
                List.of("CUSTOMER"));
    }

    private void addUser(String id, String username, String password, String fullName, String email, List<String> roles) {
        UserDto user = UserDto.builder()
                .id(id)
                .username(username)
                .email(email)
                .fullName(fullName)
                .roles(roles)
                .active(true)
                .build();
        userStore.put(username, user);
        passwordStore.put(username, password);
    }

    // ─── ORM Methods ────────────────────────────────────────────────────────
    public Optional<UserDto> findUserByUsername(String username) {
        return Optional.ofNullable(userStore.get(username));
    }

    public boolean existsByUsername(String username) {
        return userStore.containsKey(username);
    }

    public Optional<String> findByUsername(String username) {
        if (userStore.containsKey(username)) {
            return Optional.of(username);
        }
        return Optional.empty();
    }

    // ─── Native SQL Query Simulation ───────────────────────────────────────
    public Optional<String> findPasswordByUsernameNative(String username) {
        return Optional.ofNullable(passwordStore.get(username));
    }

    public Optional<UserRole> findUserRoleNative(String username) {
        if ("admin".equalsIgnoreCase(username)) {
            return Optional.of(UserRole.ADMIN);
        }
        return Optional.of(UserRole.USER);
    }
}
