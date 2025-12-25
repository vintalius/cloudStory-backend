package com.cloudstory.backend.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

public class UserResponse {

    @Schema(example = "1", description = "User ID")
    private Integer id;

    @Schema(example = "mapler123", description = "Username")
    private String name;

    @Schema(example = "player@example.com", description = "Email address")
    private String email;

    @Schema(example = "500", description = "NX Cash balance")
    private Integer paypalNX;

    @Schema(example = "1000", description = "Maple Points balance")
    private Integer mPoints;

    @Schema(example = "50", description = "Vote Points balance")
    private Integer vPoints;

    @Schema(example = "2024-12-23T10:30:00", description = "Account creation date")
    private LocalDateTime createdAt;

    // Constructor
    public UserResponse(Integer id, String name, String email, Integer paypalNX, Integer mPoints, Integer vPoints, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.paypalNX = paypalNX;
        this.mPoints = mPoints;
        this.vPoints = vPoints;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getPaypalNX() { return paypalNX; }
    public void setPaypalNX(Integer paypalNX) { this.paypalNX = paypalNX; }

    public Integer getMPoints() { return mPoints; }
    public void setMPoints(Integer mPoints) { this.mPoints = mPoints; }

    public Integer getVPoints() { return vPoints; }
    public void setVPoints(Integer vPoints) { this.vPoints = vPoints; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
