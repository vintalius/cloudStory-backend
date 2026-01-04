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

    @Column(name = "nxCredit")
    private Integer nxCredit = 0;

    @Column(name = "nxPrepaid")
    private Integer nxPrepaid = 0;

    @Column(name = "maplePoint")
    private Integer mPoints = 0;

    @Column(name = "votepoints")
    private Integer vPoints = 0;

    @Column(length = 20)
    private String voteTier = "NONE";

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

    public Integer getNxCredit() { return nxCredit; }
    public void setNxCredit(Integer nxCredit) { this.nxCredit = nxCredit; }

    public Integer getNxPrepaid() { return nxPrepaid; }
    public void setNxPrepaid(Integer nxPrepaid) { this.nxPrepaid = nxPrepaid; }

    public Integer getMPoints() { return mPoints; }
    public void setMPoints(Integer mPoints) { this.mPoints = mPoints; }

    public Integer getVPoints() { return vPoints; }
    public void setVPoints(Integer vPoints) { this.vPoints = vPoints; }

    public String getVoteTier() { return voteTier; }
    public void setVoteTier(String voteTier) { this.voteTier = voteTier; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
