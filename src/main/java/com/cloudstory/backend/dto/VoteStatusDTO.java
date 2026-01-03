package com.cloudstory.backend.dto;

import java.time.LocalDateTime;

public class VoteStatusDTO {
    
    private boolean canVote;
    private long secondsRemaining;  // 0 if can vote
    private LocalDateTime nextVoteAt;

    // Constructors
    public VoteStatusDTO() {
    }

    public VoteStatusDTO(boolean canVote, long secondsRemaining, LocalDateTime nextVoteAt) {
        this.canVote = canVote;
        this.secondsRemaining = secondsRemaining;
        this.nextVoteAt = nextVoteAt;
    }

    // Getters and Setters
    public boolean isCanVote() {
        return canVote;
    }

    public void setCanVote(boolean canVote) {
        this.canVote = canVote;
    }

    public long getSecondsRemaining() {
        return secondsRemaining;
    }

    public void setSecondsRemaining(long secondsRemaining) {
        this.secondsRemaining = secondsRemaining;
    }

    public LocalDateTime getNextVoteAt() {
        return nextVoteAt;
    }

    public void setNextVoteAt(LocalDateTime nextVoteAt) {
        this.nextVoteAt = nextVoteAt;
    }
}
