# Frontend Vote System Implementation Guide

## 🎯 Overview

This guide explains how to build the Vote Page and integrate voting functionality in the CloudStory frontend.

---

## 📋 Step-by-Step Implementation

### Step 1: Create Vote Links Configuration

Create a file: `src/utils/voteLinks.ts`

```typescript
// Vote site IDs and configurations
export const VOTE_SITES = {
  gtop100: {
    name: "GTOP100",
    baseUrl: "https://gtop100.com/MapleStory/server-105528",
    buildLink: (username: string) =>
      `https://gtop100.com/MapleStory/server-105528-${username}?vote=1&pingUsername=${username}`,
    cooldown: 12, // hours
  },
  topg: {
    name: "TopG",
    baseUrl: "https://topg.org/maplestory-private-servers/server-678265",
    buildLink: (username: string) =>
      `https://topg.org/maplestory-private-servers/server-678265-${username}#vote`,
    cooldown: 24, // hours
  },
  xtremetop100: {
    name: "XtremeTop100",
    baseUrl: "https://www.xtremetop100.com/in.php",
    buildLink: (username: string) =>
      `https://www.xtremetop100.com/in.php?site=YOUR_SITEID&postback=${username}`,
    cooldown: 12, // hours
  },
  arenaTop100: {
    name: "Arena-Top100",
    baseUrl: "https://www.arena-top100.com/index.php",
    buildLink: (username: string) =>
      `https://www.arena-top100.com/index.php?a=in&u=cloudstory&id=${username}`,
    cooldown: 24, // hours
  },
};

export type VoteSiteKey = keyof typeof VOTE_SITES;
```

---

### Step 2: Create Vote Status Hook

Create a file: `src/hooks/useVoteStatus.ts`

```typescript
import { useEffect, useState } from "react";
import { useAuth } from "./useAuth"; // Your auth hook

export interface VoteStatus {
  canVote: boolean;
  secondsRemaining: number;
  nextVoteAt: string | null;
}

export interface VoteStatusMap {
  [siteName: string]: VoteStatus;
}

export const useVoteStatus = () => {
  const { token, username } = useAuth();
  const [statuses, setStatuses] = useState<VoteStatusMap>({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!token) return;

    const fetchVoteStatus = async () => {
      try {
        const response = await fetch("/api/vote/status", {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        if (!response.ok) throw new Error("Failed to fetch vote status");
        const data = await response.json();
        setStatuses(data);
        setError(null);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Unknown error");
      } finally {
        setLoading(false);
      }
    };

    fetchVoteStatus();
    // Refresh every 30 seconds
    const interval = setInterval(fetchVoteStatus, 30000);
    return () => clearInterval(interval);
  }, [token]);

  return { statuses, loading, error };
};
```

---

### Step 3: Create Vote Tier Hook

Create a file: `src/hooks/useVoteTier.ts`

```typescript
import { useEffect, useState } from "react";
import { useAuth } from "./useAuth";

export interface VoteTierInfo {
  tier: string;
  totalVotes: number;
  nextTier: string | null;
  votesUntilNext: number;
  currentMultiplier: number;
  nextMultiplier: number;
}

export const useVoteTier = () => {
  const { token } = useAuth();
  const [tierInfo, setTierInfo] = useState<VoteTierInfo | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!token) return;

    const fetchTierInfo = async () => {
      try {
        const response = await fetch("/api/vote/tier", {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        if (!response.ok) throw new Error("Failed to fetch tier info");
        const data = await response.json();
        setTierInfo(data);
        setError(null);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Unknown error");
      } finally {
        setLoading(false);
      }
    };

    fetchTierInfo();
  }, [token]);

  return { tierInfo, loading, error };
};
```

---

### Step 4: Create Tier Badge Component

Create a file: `src/components/TierBadge.tsx`

```typescript
import React from "react";

interface TierBadgeProps {
  tier: string;
  multiplier: number;
}

export const TierBadge: React.FC<TierBadgeProps> = ({ tier, multiplier }) => {
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
    <div
      className={`${getTierColor()} px-6 py-3 rounded-lg text-white font-bold text-center`}
    >
      <span className="text-4xl mr-3">{getTierIcon()}</span>
      <span className="text-xl">{tier}</span>
      <span className="ml-4 text-lg">x{multiplier} Rewards</span>
    </div>
  );
};
```

---

### Step 5: Create Vote Progress Component

Create a file: `src/components/VoteProgress.tsx`

```typescript
import React from "react";

interface VoteProgressProps {
  currentVotes: number;
  nextTierVotes: number | null;
  nextTier: string | null;
}

export const VoteProgress: React.FC<VoteProgressProps> = ({
  currentVotes,
  nextTierVotes,
  nextTier,
}) => {
  if (!nextTier) {
    return (
      <div className="text-center text-green-500 text-xl font-bold">
        🎉 Max Tier Achieved! You are at the highest tier!
      </div>
    );
  }

  const progress = (currentVotes / nextTierVotes!) * 100;

  return (
    <div className="w-full">
      <div className="flex justify-between mb-2 text-sm text-gray-300">
        <span>{currentVotes} votes</span>
        <span>
          {nextTier} ({nextTierVotes} total needed)
        </span>
      </div>
      <div className="w-full bg-gray-800 rounded-full h-6 overflow-hidden border-2 border-gray-600">
        <div
          className="bg-gradient-to-r from-blue-500 to-blue-600 h-full transition-all duration-300 flex items-center justify-center"
          style={{ width: `${Math.min(progress, 100)}%` }}
        >
          {progress > 10 && (
            <span className="text-white text-xs font-bold">
              {Math.round(progress)}%
            </span>
          )}
        </div>
      </div>
    </div>
  );
};
```

---

### Step 6: Create Vote Button Component

Create a file: `src/components/VoteButton.tsx`

```typescript
import React from "react";
import { VOTE_SITES, VoteSiteKey } from "../utils/voteLinks";

interface VoteButtonProps {
  siteKey: VoteSiteKey;
  username: string;
  canVote: boolean;
  secondsRemaining: number;
}

export const VoteButton: React.FC<VoteButtonProps> = ({
  siteKey,
  username,
  canVote,
  secondsRemaining,
}) => {
  const site = VOTE_SITES[siteKey];

  const handleVoteClick = () => {
    const voteUrl = site.buildLink(username);
    window.open(voteUrl, "_blank", "width=800,height=600");
  };

  const formatTimeRemaining = (seconds: number) => {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const secs = seconds % 60;

    if (hours > 0) return `${hours}h ${minutes}m`;
    if (minutes > 0) return `${minutes}m ${secs}s`;
    return `${secs}s`;
  };

  return (
    <button
      onClick={handleVoteClick}
      disabled={!canVote}
      className={`px-6 py-3 rounded-lg font-bold transition-all ${
        canVote
          ? "bg-blue-600 hover:bg-blue-700 text-white cursor-pointer"
          : "bg-gray-600 text-gray-400 cursor-not-allowed"
      }`}
    >
      {canVote ? (
        <>Vote on {site.name} ⭐</>
      ) : (
        <>Available in {formatTimeRemaining(secondsRemaining)}</>
      )}
    </button>
  );
};
```

---

### Step 7: Create Main Vote Page

Create a file: `src/pages/VotePage.tsx`

```typescript
import React from "react";
import { useAuth } from "../hooks/useAuth";
import { useVoteStatus } from "../hooks/useVoteStatus";
import { useVoteTier } from "../hooks/useVoteTier";
import { VOTE_SITES, VoteSiteKey } from "../utils/voteLinks";
import { TierBadge } from "../components/TierBadge";
import { VoteProgress } from "../components/VoteProgress";
import { VoteButton } from "../components/VoteButton";

export const VotePage: React.FC = () => {
  const { username } = useAuth();
  const { statuses, loading: statusLoading } = useVoteStatus();
  const { tierInfo, loading: tierLoading } = useVoteTier();

  if (statusLoading || tierLoading) {
    return (
      <div className="flex justify-center items-center h-screen">
        <div className="text-white text-2xl">Loading vote information...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-900 p-8">
      <div className="max-w-4xl mx-auto">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-5xl font-bold text-white mb-2">
            Vote for CloudStory
          </h1>
          <p className="text-gray-400 text-lg">
            Help us grow! Vote on the top-list sites and earn exclusive rewards.
          </p>
        </div>

        {/* Tier Section */}
        {tierInfo && (
          <div className="mb-12 bg-gray-800 p-6 rounded-lg">
            <h2 className="text-2xl font-bold text-white mb-4">
              Your Voting Tier
            </h2>
            <TierBadge
              tier={tierInfo.tier}
              multiplier={tierInfo.currentMultiplier}
            />

            <div className="mt-6">
              <VoteProgress
                currentVotes={tierInfo.totalVotes}
                nextTierVotes={tierInfo.totalVotes + tierInfo.votesUntilNext}
                nextTier={tierInfo.nextTier}
              />
            </div>

            {tierInfo.nextTier && (
              <p className="text-gray-300 mt-4">
                {tierInfo.votesUntilNext} more votes to reach{" "}
                <strong>{tierInfo.nextTier}</strong> tier!
              </p>
            )}
          </div>
        )}

        {/* Vote Buttons Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {Object.entries(VOTE_SITES).map(([key, site]) => {
            const status =
              statuses[site.name.toLowerCase().replace(/[^a-z0-9]/g, "")];

            return (
              <div
                key={key}
                className="bg-gray-800 p-6 rounded-lg border-2 border-gray-700 hover:border-blue-500 transition-all"
              >
                <h3 className="text-2xl font-bold text-white mb-2">
                  {site.name}
                </h3>
                <p className="text-gray-400 mb-4">
                  Cooldown: Every {site.cooldown} hours
                </p>

                {status && (
                  <>
                    <VoteButton
                      siteKey={key as VoteSiteKey}
                      username={username || ""}
                      canVote={status.canVote}
                      secondsRemaining={status.secondsRemaining}
                    />

                    {!status.canVote && (
                      <div className="mt-3 text-center text-yellow-400 text-sm">
                        ⏰ Next vote available: {status.nextVoteAt}
                      </div>
                    )}
                  </>
                )}
              </div>
            );
          })}
        </div>

        {/* Rewards Info */}
        <div className="mt-12 bg-gray-800 p-6 rounded-lg">
          <h2 className="text-2xl font-bold text-white mb-4">💰 Rewards</h2>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div className="bg-gray-700 p-4 rounded text-center">
              <p className="text-gray-400 text-sm">Base NX (No Tier)</p>
              <p className="text-white text-xl font-bold">5,000 - 8,000</p>
            </div>
            <div className="bg-amber-900 p-4 rounded text-center">
              <p className="text-gray-300 text-sm">🥉 Bronze (x3)</p>
              <p className="text-white text-xl font-bold">15,000 - 24,000</p>
            </div>
            <div className="bg-gray-500 p-4 rounded text-center">
              <p className="text-gray-300 text-sm">🥈 Silver (x5)</p>
              <p className="text-white text-xl font-bold">25,000 - 40,000</p>
            </div>
            <div className="bg-yellow-600 p-4 rounded text-center">
              <p className="text-gray-300 text-sm">🥇 Gold (x10)</p>
              <p className="text-white text-xl font-bold">50,000 - 80,000</p>
            </div>
          </div>
          <p className="text-gray-300 mt-4 text-sm">
            Plus 1-2 Vote Points per vote (not multiplied by tier)
          </p>
        </div>
      </div>
    </div>
  );
};
```

---

## 🔄 Auto-Refresh Logic

Add this to refresh tier and status after voting:

```typescript
// In VotePage.tsx, after vote button click
const handleVoteComplete = () => {
  // Refresh tier info
  setTimeout(() => {
    fetchTierInfo();
    fetchVoteStatus();
  }, 5000); // Wait 5 seconds then refresh
};
```

---

## 📡 API Endpoints Used

```
GET /api/vote/status
  Response: { [siteName]: { canVote, secondsRemaining, nextVoteAt } }

GET /api/vote/tier
  Response: { tier, totalVotes, nextTier, votesUntilNext, currentMultiplier, nextMultiplier }

GET /api/vote/count
  Response: { totalVotes }

GET /api/vote/history
  Response: { votes: [...], totalVotes }
```

---

## ✅ Checklist

- [ ] Create `voteLinks.ts` config file
- [ ] Create `useVoteStatus.ts` hook
- [ ] Create `useVoteTier.ts` hook
- [ ] Create `TierBadge.tsx` component
- [ ] Create `VoteProgress.tsx` component
- [ ] Create `VoteButton.tsx` component
- [ ] Create `VotePage.tsx` main page
- [ ] Add routing to `/vote` page
- [ ] Test all 4 voting sites
- [ ] Verify tier calculations
- [ ] Add to navigation menu
