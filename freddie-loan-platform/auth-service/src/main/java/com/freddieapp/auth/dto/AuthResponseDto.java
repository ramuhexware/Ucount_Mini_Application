package com.freddieapp.auth.dto;

import java.util.List;

public class AuthResponseDto {

    private String token;
    private String username;
    private List<String> roles;
    private boolean authenticated;

    public AuthResponseDto() {}

    public AuthResponseDto(String token, String username, List<String> roles, boolean authenticated) {
        this.token = token;
        this.username = username;
        this.roles = roles;
        this.authenticated = authenticated;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    public boolean isAuthenticated() { return authenticated; }
    public void setAuthenticated(boolean authenticated) { this.authenticated = authenticated; }
}
