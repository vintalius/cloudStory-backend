# Voting Tiers System - Implementation Plan

## Overview

This document outlines the complete implementation plan for a voting rewards tier system that gives multipliers to loyal voters.

### Tier Structure

| Tier          | Votes Required | Multiplier | Base Reward | Reward with Tier |
| ------------- | -------------- | ---------- | ----------- | ---------------- |
| 🥉 **BRONZE** | 10+            | x3         | 5000 NX     | **15,000 NX**    |
| 🥈 **SILVER** | 50+            | x5         | 5000 NX     | **25,000 NX**    |
| 🥇 **GOLD**   | 100+           | x10        | 5000 NX     | **50,000 NX**    |
| ⚪ **NONE**   | 0-9            | x1         | 5000 NX     | **5,000 NX**     |

---

## Database Changes

### 1. Add voteTier Column to accounts Table

```sql
-- Add voting tier column to track user's current tier
ALTER TABLE accounts
ADD COLUMN voteTier VARCHAR(20) DEFAULT 'NONE'
COMMENT 'Current voting tier: NONE, BRONZE, SILVER, GOLD';

-- Create index for faster tier queries
CREATE INDEX idx_vote_tier ON accounts(voteTier);
```

### 2. Database Schema After Changes

```sql
accounts table:
- id (existing)
- name (existing)
- paypalNX (existing)
- vPoints (existing)
- mPoints (existing)
- voteTier (NEW) - stores current tier status

votes table (no changes):
- id
- username
- vote_site
- voted_at
- ip_address
- nx_rewarded
- vote_points_rewarded
```

---

## Backend Implementation

### Phase 1: Create VoteTier Enum

**File:** `src/main/java/com/cloudstory/backend/enums/VoteTier.java`

```java
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
```

### Phase 2: Create VoteTierDTO

**File:** `src/main/java/com/cloudstory/backend/dto/VoteTierDTO.java`

```java
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
```

### Phase 3: Add voteTier Field to Account Entity

**File:** `src/main/java/com/cloudstory/backend/entity/Account.java`

```java
// Add this field to the Account entity

@Column(length = 20)
private String voteTier = "NONE";

// Add getter and setter
public String getVoteTier() {
    return voteTier;
}

public void setVoteTier(String voteTier) {
    this.voteTier = voteTier;
}
```

### Phase 4: Update VoteService with Tier Logic

**File:** `src/main/java/com/cloudstory/backend/service/VoteService.java`

```java
// Add new methods to VoteService

/**
 * Calculate and update user's voting tier based on total votes
 */
private void updateUserTier(Account account) {
    long totalVotes = voteRepository.countByUsername(account.getName());
    VoteTier newTier = VoteTier.calculateTier(totalVotes);
    account.setVoteTier(newTier.name());
    accountRepository.save(account);
}

/**
 * Get current tier multiplier for user
 */
private int getTierMultiplier(Account account) {
    try {
        VoteTier tier = VoteTier.valueOf(account.getVoteTier());
        return tier.getMultiplier();
    } catch (IllegalArgumentException e) {
        return 1; // Default to NONE tier
    }
}

/**
 * Get user's voting tier information
 */
public VoteTierDTO getUserTierInfo(String username) {
    long totalVotes = voteRepository.countByUsername(username);
    VoteTier currentTier = VoteTier.calculateTier(totalVotes);
    VoteTier nextTier = currentTier.getNextTier();

    long votesUntilNext = 0;
    int nextMultiplier = currentTier.getMultiplier();

    if (nextTier != null) {
        votesUntilNext = nextTier.getVotesRequired() - totalVotes;
        nextMultiplier = nextTier.getMultiplier();
    }

    return new VoteTierDTO(
        currentTier.name(),
        totalVotes,
        nextTier != null ? nextTier.name() : null,
        votesUntilNext,
        currentTier.getMultiplier(),
        nextMultiplier
    );
}

/**
 * Modified processVote method with tier multiplier
 */
@Transactional
public void processVote(String username, String voteSite, String secretKey, String ipAddress) {

    // 1. Verify secret key
    if (!isValidSecretKey(voteSite, secretKey)) {
        throw new RuntimeException("Invalid secret key for site: " + voteSite);
    }

    // 2. Check cooldown
    VoteStatusDTO status = getVoteStatus(username, voteSite);
    if (!status.isCanVote()) {
        throw new RuntimeException("Vote cooldown not expired. Try again in " +
            status.getSecondsRemaining() + " seconds");
    }

    // 3. Find user account
    Account account = accountRepository.findByName(username)
        .orElseThrow(() -> new RuntimeException("User not found: " + username));

    // 4. Calculate base rewards
    int baseNxReward = NX_REWARDS.getOrDefault(voteSite, 5000);
    int vpReward = VP_REWARDS.getOrDefault(voteSite, 1);

    // 5. Apply tier multiplier to NX reward
    int tierMultiplier = getTierMultiplier(account);
    int finalNxReward = baseNxReward * tierMultiplier;

    // 6. Give rewards to account
    int currentNX = (account.getPaypalNX() != null) ? account.getPaypalNX() : 0;
    int currentVP = (account.getVPoints() != null) ? account.getVPoints() : 0;

    account.setPaypalNX(currentNX + finalNxReward);
    account.setVPoints(currentVP + vpReward);
    accountRepository.save(account);

    // 7. Save vote record
    Vote vote = new Vote();
    vote.setUsername(username);
    vote.setVoteSite(voteSite);
    vote.setVotedAt(LocalDateTime.now());
    vote.setIpAddress(ipAddress);
    vote.setNxRewarded(finalNxReward);  // Save actual rewarded amount
    vote.setVotePointsRewarded(vpReward);
    voteRepository.save(vote);

    // 8. Update user's tier after voting
    updateUserTier(account);
}
```

### Phase 5: Add Tier Endpoint to VoteController

**File:** `src/main/java/com/cloudstory/backend/controller/VoteController.java`

```java
// Add this endpoint to VoteController

/**
 * Get user's voting tier information
 *
 * @param authHeader Authorization header with JWT token
 * @return User's tier information including progress to next tier
 */
@GetMapping("/tier")
public ResponseEntity<?> getUserTier(@RequestHeader("Authorization") String authHeader) {
    try {
        String username = getUsernameFromToken(authHeader);
        VoteTierDTO tierInfo = voteService.getUserTierInfo(username);

        return ResponseEntity.ok(tierInfo);
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}
```

---

## API Endpoints Summary

### Existing Endpoints (Updated)

- `GET /api/vote/status` - Returns vote cooldown status (no changes)
- `POST /api/vote/callback/{site}` - Voting site callback (now applies tier multiplier)
- `GET /api/vote/history` - User's vote history (no changes)
- `GET /api/vote/count` - Total vote count (no changes)

### New Endpoints

- `GET /api/vote/tier` - Get user's tier information

**Example Response:**

```json
{
  "tier": "BRONZE",
  "totalVotes": 15,
  "nextTier": "SILVER",
  "votesUntilNext": 35,
  "currentMultiplier": 3,
  "nextMultiplier": 5
}
```

---

## Frontend Implementation

### Phase 1: Create Tier Badge Component

**File:** `src/components/TierBadge.tsx`

```tsx
interface TierBadgeProps {
  tier: string;
  multiplier: number;
}

const TierBadge: React.FC<TierBadgeProps> = ({ tier, multiplier }) => {
  const getTierColor = () => {
    switch (tier) {
      case "GOLD":
        return "bg-yellow-500";
      case "SILVER":
        return "bg-gray-400";
      case "BRONZE":
        return "bg-amber-700";
      default:
        return "bg-gray-600";
    }
  };

  const getTierIcon = () => {
    switch (tier) {
      case "GOLD":
        return "🥇";
      case "SILVER":
        return "🥈";
      case "BRONZE":
        return "🥉";
      default:
        return "⚪";
    }
  };

  return (
    <div className={`${getTierColor()} px-4 py-2 rounded-lg text-white`}>
      <span className="text-2xl mr-2">{getTierIcon()}</span>
      <span className="font-bold">{tier}</span>
      <span className="ml-2">x{multiplier} Rewards</span>
    </div>
  );
};
```

### Phase 2: Create Tier Progress Component

**File:** `src/components/TierProgress.tsx`

```tsx
interface TierProgressProps {
  currentVotes: number;
  nextTierVotes: number;
  nextTier: string | null;
}

const TierProgress: React.FC<TierProgressProps> = ({
  currentVotes,
  nextTierVotes,
  nextTier,
}) => {
  if (!nextTier) {
    return <div className="text-green-500">🎉 Max Tier Achieved!</div>;
  }

  const progress = (currentVotes / nextTierVotes) * 100;

  return (
    <div className="w-full">
      <div className="flex justify-between mb-2">
        <span>{currentVotes} votes</span>
        <span>
          Next: {nextTier} ({nextTierVotes} votes)
        </span>
      </div>
      <div className="w-full bg-gray-700 rounded-full h-4">
        <div
          className="bg-blue-500 h-4 rounded-full transition-all"
          style={{ width: `${progress}%` }}
        />
      </div>
    </div>
  );
};
```

### Phase 3: Update Vote Page

**File:** `src/pages/VotePage.tsx`

```tsx
// Add state for tier info
const [tierInfo, setTierInfo] = useState(null);

// Fetch tier info
useEffect(() => {
  const fetchTierInfo = async () => {
    const response = await fetch("/api/vote/tier", {
      headers: { Authorization: `Bearer ${token}` },
    });
    const data = await response.json();
    setTierInfo(data);
  };

  fetchTierInfo();
}, []);

// Display tier badge and progress
return (
  <div>
    {tierInfo && (
      <div className="mb-6">
        <TierBadge
          tier={tierInfo.tier}
          multiplier={tierInfo.currentMultiplier}
        />
        <TierProgress
          currentVotes={tierInfo.totalVotes}
          nextTierVotes={tierInfo.totalVotes + tierInfo.votesUntilNext}
          nextTier={tierInfo.nextTier}
        />
      </div>
    )}
    {/* Existing vote buttons */}
  </div>
);
```

### Phase 4: Update Dashboard

**File:** `src/pages/Dashboard.tsx`

```tsx
// Display tier badge in user stats section
<div className="stats-card">
  <h3>Voting Stats</h3>
  {tierInfo && (
    <TierBadge tier={tierInfo.tier} multiplier={tierInfo.currentMultiplier} />
  )}
  <p>Total Votes: {tierInfo?.totalVotes}</p>
</div>
```

---

## User Flow Example

### New User Journey

```
Day 1: New Player
├─ Votes: 0
├─ Tier: NONE (x1)
└─ Receives: 5,000 NX per vote

Day 5: Regular Voter
├─ Votes: 10
├─ Tier: BRONZE (x3) 🎉
├─ Message: "Congratulations! You've reached BRONZE tier!"
└─ Receives: 15,000 NX per vote (5000 × 3)

Day 25: Active Voter
├─ Votes: 50
├─ Tier: SILVER (x5) 🎉
├─ Message: "Amazing! You've reached SILVER tier!"
└─ Receives: 25,000 NX per vote (5000 × 5)

Day 50: Top Voter
├─ Votes: 100
├─ Tier: GOLD (x10) 🎉
├─ Message: "Legendary! You've reached GOLD tier!"
└─ Receives: 50,000 NX per vote (5000 × 10)
```

---

## Testing Checklist

### Backend Tests

- [ ] Tier calculation logic works correctly
- [ ] Multiplier applies to NX rewards
- [ ] Tier updates after each vote
- [ ] API endpoint returns correct tier info
- [ ] Edge cases (0 votes, exactly at threshold)

### Frontend Tests

- [ ] Tier badge displays correctly
- [ ] Progress bar updates in real-time
- [ ] Tier upgrade animation works
- [ ] Mobile responsive design
- [ ] Accessibility (screen readers)

### Integration Tests

- [ ] Full vote flow with tier multiplier
- [ ] Tier upgrade triggers correctly
- [ ] Database consistency
- [ ] Performance with large vote counts

---

## Deployment Steps

1. **Backend:**

   - Run SQL migration to add voteTier column
   - Deploy updated backend code
   - Verify API endpoints work

2. **Frontend:**

   - Deploy new components
   - Test tier display
   - Verify vote flow

3. **Database:**

   - Backfill existing users with correct tiers
   - Create indexes for performance

4. **Monitoring:**
   - Track tier distribution
   - Monitor API performance
   - Check for edge cases

---

## Future Enhancements

- [ ] Tier decay (lose tier if inactive)
- [ ] Special tier-exclusive rewards
- [ ] Leaderboard for top tiers
- [ ] Achievement badges for tiers
- [ ] Email notifications on tier upgrade
- [ ] Tier-based voting cooldown reduction
