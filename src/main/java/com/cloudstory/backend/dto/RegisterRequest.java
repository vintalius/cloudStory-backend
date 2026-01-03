package com.cloudstory.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    
    @NotBlank(message = "Username is required")
    @Pattern(regexp = "^(?=.*[a-zA-Z])[a-zA-Z0-9_-]{3,20}$", message = "Username must be 3-20 characters, contain at least one letter, and only letters, numbers, underscores, or hyphens")
    @Schema(example = "mapler123", description = "The user's login name (3-20 characters, must include at least one letter)")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password is too short. Please use at least 8 characters for better security")
    @Schema(example = "MySecretPass!1", description = "The user's password (minimum 8 characters)")
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format. Please enter a valid email address (e.g., example@domain.com)")
    @Schema(example = "player@example.com", description = "Valid email address")
    private String email;

    @Schema(example = "1999-01-01", description = "Birth date in YYYY-MM-DD (optional)")
    private String birthday;

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getBirthday() { return birthday; }
    public void setBirthday(String birthday) { this.birthday = birthday; }
}
