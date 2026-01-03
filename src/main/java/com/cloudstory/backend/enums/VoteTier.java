package com.cloudstory.backend.enums;

public enum VoteTier {
    NONE(0, 1),
    BRONZE(10, 3),
    SILVER(50, 5),
    GOLD(100, 10);

    private final int votesRequired;
    private final int multiplier;

    VoteTier(int votesRequired, int multiplier) {
        this.votesRequired = votesRequired;
        this.multiplier = multiplier;
    }

    public int getVotesRequired() {
        return votesRequired;
    }

    public int getMultiplier() {
        return multiplier;
    }

    /**
     * Calculate tier based on total votes
     */
    public static VoteTier calculateTier(long totalVotes) {
        if (totalVotes >= GOLD.votesRequired) return GOLD;
        if (totalVotes >= SILVER.votesRequired) return SILVER;
        if (totalVotes >= BRONZE.votesRequired) return BRONZE;
        return NONE;
    }

    /**
     * Get next tier or null if already at max
     */
    public VoteTier getNextTier() {
        return switch (this) {
            case NONE -> BRONZE;
            case BRONZE -> SILVER;
            case SILVER -> GOLD;
            case GOLD -> null;
        };
    }
}
