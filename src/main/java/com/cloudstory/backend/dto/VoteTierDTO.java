package com.cloudstory.backend.dto;

public class VoteTierDTO {
    private String tier;              // Current tier name
    private long totalVotes;          // Total votes by user
    private String nextTier;          // Next tier name (null if GOLD)
    private long votesUntilNext;      // Votes needed for next tier
    private int currentMultiplier;    // Current reward multiplier
    private int nextMultiplier;       // Next tier multiplier

    // Constructor
    public VoteTierDTO(String tier, long totalVotes, String nextTier,
                       long votesUntilNext, int currentMultiplier, int nextMultiplier) {
        this.tier = tier;
        this.totalVotes = totalVotes;
        this.nextTier = nextTier;
        this.votesUntilNext = votesUntilNext;
        this.currentMultiplier = currentMultiplier;
        this.nextMultiplier = nextMultiplier;
    }

    // Getters and Setters
    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }

    public long getTotalVotes() { return totalVotes; }
    public void setTotalVotes(long totalVotes) { this.totalVotes = totalVotes; }

    public String getNextTier() { return nextTier; }
    public void setNextTier(String nextTier) { this.nextTier = nextTier; }

    public long getVotesUntilNext() { return votesUntilNext; }
    public void setVotesUntilNext(long votesUntilNext) { this.votesUntilNext = votesUntilNext; }

    public int getCurrentMultiplier() { return currentMultiplier; }
    public void setCurrentMultiplier(int currentMultiplier) { this.currentMultiplier = currentMultiplier; }

    public int getNextMultiplier() { return nextMultiplier; }
    public void setNextMultiplier(int nextMultiplier) { this.nextMultiplier = nextMultiplier; }
}
