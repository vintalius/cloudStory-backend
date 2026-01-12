package com.cloudstory.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "pending_nx")
public class PendingNx {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "account_id", nullable = false)
    private Integer accountId;

    @Column(name = "username", nullable = false, length = 13)
    private String username;

    @Column(name = "nx_credit", nullable = false)
    private Integer nxCredit = 0;

    @Column(name = "nx_prepaid", nullable = false)
    private Integer nxPrepaid = 0;

    @Column(name = "maple_points", nullable = false)
    private Integer maplePoints = 0;

    @Column(name = "vote_points", nullable = false)
    private Integer votePoints = 0;

    @Column(name = "reward_type", length = 50)
    private String rewardType = "VOTE";

    @Column(name = "reward_site", length = 50)
    private String rewardSite;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "applied")
    private Boolean applied = false;

    @Column(name = "applied_at")
    private LocalDateTime appliedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
