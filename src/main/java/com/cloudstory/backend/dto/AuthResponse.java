package com.cloudstory.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    
    @Schema(example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", description = "JWT Token")
    private String token;

    @Schema(example = "Bearer", description = "Token Type")
    private String type;

    @Schema(example = "mapler123", description = "Username")
    private String username;

    public AuthResponse(String token, String username) {
        this.token = token;
        this.type = "Bearer";
        this.username = username;
    }
}
