package com.freddieapp.auth.auth;

import com.freddieapp.auth.dto.UserDto;
import org.springframework.stereotype.Component;

@Component
public class AuthContext {

    private static final ThreadLocal<UserDto> CURRENT_USER = new ThreadLocal<>();

    public static void setCurrentUser(UserDto user) {
        CURRENT_USER.set(user);
    }

    public static UserDto getCurrentUser() {
        return CURRENT_USER.get();
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
