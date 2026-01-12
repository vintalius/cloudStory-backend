package com.cloudstory.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PendingNxDTO {
    private Integer id;
    private String username;
    private Integer nxCredit;
    private Integer votePoints;
    private String rewardType;
    private String rewardSite;
    private LocalDateTime createdAt;
    private Boolean applied;
}
