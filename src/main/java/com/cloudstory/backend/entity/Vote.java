package com.cloudstory.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "votes")
public class Vote {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(name = "vote_site", nullable = false, length = 50)
    private String voteSite;

    @Column(name = "voted_at", nullable = false)
    private LocalDateTime votedAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "nx_rewarded")
    private Integer nxRewarded;

    @Column(name = "vote_points_rewarded")
    private Integer votePointsRewarded;

    // Constructors
    public Vote() {
    }

    public Vote(String username, String voteSite, LocalDateTime votedAt, String ipAddress, 
                Integer nxRewarded, Integer votePointsRewarded) {
        this.username = username;
        this.voteSite = voteSite;
        this.votedAt = votedAt;
        this.ipAddress = ipAddress;
        this.nxRewarded = nxRewarded;
        this.votePointsRewarded = votePointsRewarded;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getVoteSite() {
        return voteSite;
    }

    public void setVoteSite(String voteSite) {
        this.voteSite = voteSite;
    }

    public LocalDateTime getVotedAt() {
        return votedAt;
    }

    public void setVotedAt(LocalDateTime votedAt) {
        this.votedAt = votedAt;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Integer getNxRewarded() {
        return nxRewarded;
    }

    public void setNxRewarded(Integer nxRewarded) {
        this.nxRewarded = nxRewarded;
    }

    public Integer getVotePointsRewarded() {
        return votePointsRewarded;
    }

    public void setVotePointsRewarded(Integer votePointsRewarded) {
        this.votePointsRewarded = votePointsRewarded;
    }
}
