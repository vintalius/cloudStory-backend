package com.cloudstory.backend.service;

import com.cloudstory.backend.dto.VoteStatusDTO;
import com.cloudstory.backend.dto.VoteTierDTO;
import com.cloudstory.backend.entity.Account;
import com.cloudstory.backend.entity.PendingNx;
import com.cloudstory.backend.entity.Vote;
import com.cloudstory.backend.enums.VoteTier;
import com.cloudstory.backend.repository.AccountRepository;
import com.cloudstory.backend.repository.PendingNxRepository;
import com.cloudstory.backend.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
public class VoteService {

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PendingNxRepository pendingNxRepository;

    // Inject secret keys from application.properties
    @Value("${vote.secret.gtop100:}")
    private String gtop100Secret;

    @Value("${vote.secret.topg:}")
    private String topgSecret;

    @Value("${vote.secret.xtremetop100:}")
    private String xtremetop100Secret;

    @Value("${vote.secret.arena-top100:}")
    private String arenaTop100Secret;

    // Cooldown times in hours for each site
    private static final Map<String, Integer> COOLDOWNS = Map.of(
        "gtop100", 12,
        "topg", 24,
        "xtremetop100", 12,
        "arena-top100", 12
    );

    // NX rewards for each site
    private static final Map<String, Integer> NX_REWARDS = Map.of(
        "gtop100", 7000,
        "topg", 8000,
        "xtremetop100", 5000,
        "arena-top100", 7000
    );

    // Vote Points rewards for each site
    private static final Map<String, Integer> VP_REWARDS = Map.of(
        "gtop100", 1,
        "topg", 2,
        "xtremetop100", 1,
        "arena-top100", 1
    );

    /**
     * Check if user can vote for a specific site
     * @param username The username
     * @param voteSite The voting site (e.g., "gtop100", "topg")
     * @return VoteStatusDTO with cooldown information
     */
    public VoteStatusDTO getVoteStatus(String username, String voteSite) {
        Optional<Vote> lastVote = voteRepository
            .findTopByUsernameAndVoteSiteOrderByVotedAtDesc(username, voteSite);

        if (lastVote.isEmpty()) {
            return new VoteStatusDTO(true, 0, null); // Can vote - never voted before
        }

        LocalDateTime votedAt = lastVote.get().getVotedAt();
        int cooldownHours = COOLDOWNS.getOrDefault(voteSite, 12);
        LocalDateTime canVoteAt = votedAt.plusHours(cooldownHours);

        if (LocalDateTime.now().isAfter(canVoteAt)) {
            return new VoteStatusDTO(true, 0, null); // Cooldown passed - can vote
        }

        // Still on cooldown
        long secondsRemaining = Duration.between(LocalDateTime.now(), canVoteAt).getSeconds();
        return new VoteStatusDTO(false, secondsRemaining, canVoteAt);
    }

    /**
     * Process vote callback from voting site
     * @param username The username who voted
     * @param voteSite The voting site name
     * @param secretKey The secret key from the voting site (can be null for sites that don't require it)
     * @param ipAddress The IP address of the voter
     * @param voteSuccess Optional: whether vote was successful (for sites like Arena-Top100)
     * @throws RuntimeException if validation fails
     */
    @Transactional
    public void processVote(String username, String voteSite, String secretKey, String ipAddress) {
        processVote(username, voteSite, secretKey, ipAddress, true);
    }

    @Transactional
    public void processVote(String username, String voteSite, String secretKey, String ipAddress, boolean voteSuccess) {
        
        // 1. Verify secret key only for sites that require it
        if ("gtop100".equals(voteSite) || "arena-top100".equals(voteSite)) {
            if (!isValidSecretKey(voteSite, secretKey)) {
                throw new RuntimeException("Invalid secret key for site: " + voteSite);
            }
        }

        // 2. Check if vote was successful (for Arena-Top100, 1=success, 0=failed)
        if (!voteSuccess) {
            throw new RuntimeException("Vote was not successful on " + voteSite);
        }

        // 3. Check cooldown
        VoteStatusDTO status = getVoteStatus(username, voteSite);
        if (!status.isCanVote()) {
            throw new RuntimeException("Vote cooldown not expired. Try again in " + 
                status.getSecondsRemaining() + " seconds");
        }

        // 4. Find user account
        Account account = accountRepository.findByName(username)
            .orElseThrow(() -> new RuntimeException("User not found: " + username));
        
        System.out.println("DEBUG: Found account: " + account.getName() + " (ID: " + account.getId() + ")");

        // 5. Calculate base rewards
        int baseNxReward = NX_REWARDS.getOrDefault(voteSite, 5000);
        int vpReward = VP_REWARDS.getOrDefault(voteSite, 1);
        
        System.out.println("DEBUG: Base rewards - NX: " + baseNxReward + ", VP: " + vpReward);

        // 6. Apply tier multiplier to NX reward
        int tierMultiplier = getTierMultiplier(account);
        int finalNxReward = baseNxReward * tierMultiplier;

        // 7. Save rewards to PENDING_NX table (not directly to accounts)
        // This prevents Cosmic from overwriting the NX when player logs out
        // Cosmic will read from pending_nx on login and apply the rewards
        PendingNx pendingReward = new PendingNx();
        pendingReward.setAccountId(account.getId());
        pendingReward.setUsername(username);
        pendingReward.setNxCredit(finalNxReward);
        pendingReward.setVotePoints(vpReward);
        pendingReward.setRewardType("VOTE");
        pendingReward.setRewardSite(voteSite);
        pendingReward.setApplied(false);
        
        pendingNxRepository.save(pendingReward);
        
        System.out.println("DEBUG: Pending reward saved to pending_nx table:");
        System.out.println("  - Account ID: " + account.getId());
        System.out.println("  - Username: " + username);
        System.out.println("  - NX Credit: " + finalNxReward + " (x" + tierMultiplier + " multiplier)");
        System.out.println("  - Vote Points: " + vpReward);
        System.out.println("  - Site: " + voteSite);
        System.out.println("  - Will be applied on next login");

        // 8. Save vote record
        Vote vote = new Vote();
        vote.setUsername(username);
        vote.setVoteSite(voteSite);
        vote.setVotedAt(LocalDateTime.now());
        vote.setIpAddress(ipAddress);
        vote.setNxRewarded(finalNxReward);  // Save actual rewarded amount
        vote.setVotePointsRewarded(vpReward);
        voteRepository.save(vote);
        
        System.out.println("DEBUG: Vote saved to database");

        // 9. Update user's tier after voting
        updateUserTier(account);
        
        System.out.println("DEBUG: User tier updated");
    }

    /**
     * Verify that the secret key matches the configured key for the voting site
     * @param voteSite The voting site name
     * @param providedKey The secret key provided by the voting site
     * @return true if valid, false otherwise
     */
    private boolean isValidSecretKey(String voteSite, String providedKey) {
        if (providedKey == null || providedKey.isEmpty()) {
            return false;
        }

        String expectedKey = switch (voteSite.toLowerCase()) {
            case "gtop100" -> gtop100Secret;
            case "topg" -> topgSecret;
            case "xtremetop100" -> xtremetop100Secret;
            case "arena-top100" -> arenaTop100Secret;
            default -> "";
        };

        return !expectedKey.isEmpty() && expectedKey.equals(providedKey);
    }

    /**
     * Get all votes by a specific user
     * @param username The username
     * @return List of all votes
     */
    public java.util.List<Vote> getUserVoteHistory(String username) {
        return voteRepository.findByUsernameOrderByVotedAtDesc(username);
    }

    /**
     * Get total vote count for a user
     * @param username The username
     * @return Total number of votes
     */
    public long getUserVoteCount(String username) {
        return voteRepository.countByUsername(username);
    }

    /**
     * Calculate and update user's voting tier based on total votes
     */
    private void updateUserTier(Account account) {
        if (account == null || account.getName() == null) {
            throw new RuntimeException("Account or account name is null");
        }
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
            String tier = account.getVoteTier();
            if (tier == null || tier.isEmpty()) {
                tier = "NONE"; // Default tier
            }
            VoteTier voteTier = VoteTier.valueOf(tier);
            return voteTier.getMultiplier();
        } catch (IllegalArgumentException | NullPointerException e) {
            return 1; // Default to NONE tier multiplier
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
}
