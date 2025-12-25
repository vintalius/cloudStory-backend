package com.cloudstory.backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "password")
    private String password;

    @Column(name = "email")
    private String email;

    @Column(name = "birthday")
    private String birthday;

    @Column(name = "loggedin")
    private Integer loggedin;

    @Column(name = "gm")
    private Integer gm;

    @Column(name = "paypalNX")
    private Integer paypalNX;

    @Column(name = "mPoints")
    private Integer mPoints;

    @Column(name = "vPoints")
    private Integer vPoints;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getBirthday() { return birthday; }
    public void setBirthday(String birthday) { this.birthday = birthday; }

    public Integer getLoggedin() { return loggedin; }
    public void setLoggedin(Integer loggedin) { this.loggedin = loggedin; }

    public Integer getGm() { return gm; }
    public void setGm(Integer gm) { this.gm = gm; }

    public Integer getPaypalNX() { return paypalNX; }
    public void setPaypalNX(Integer paypalNX) { this.paypalNX = paypalNX; }

    public Integer getMPoints() { return mPoints; }
    public void setMPoints(Integer mPoints) { this.mPoints = mPoints; }

    public Integer getVPoints() { return vPoints; }
    public void setVPoints(Integer vPoints) { this.vPoints = vPoints; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
