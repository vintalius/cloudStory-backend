package com.cloudstory.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class LoginRequest {
    
    @Schema(example = "mapler123", description = "Username (not email)")
    private String username;

    @Schema(example = "MySecretPass!1", description = "Password")
    private String password;

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
