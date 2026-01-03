package com.cloudstory.backend.service;

import com.cloudstory.backend.dto.VoteStatusDTO;
import com.cloudstory.backend.entity.Account;
import com.cloudstory.backend.entity.Vote;
import com.cloudstory.backend.repository.AccountRepository;
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
        "gtop100", 5000,
        "topg", 8000,
        "xtremetop100", 5000,
        "arena-top100", 5000
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
     * @param secretKey The secret key from the voting site
     * @param ipAddress The IP address of the voter
     * @throws RuntimeException if validation fails
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

        // 4. Calculate rewards
        int nxReward = NX_REWARDS.getOrDefault(voteSite, 5000);
        int vpReward = VP_REWARDS.getOrDefault(voteSite, 1);

        // 5. Give rewards to account
        int currentNX = (account.getPaypalNX() != null) ? account.getPaypalNX() : 0;
        int currentVP = (account.getVPoints() != null) ? account.getVPoints() : 0;
        
        account.setPaypalNX(currentNX + nxReward);
        account.setVPoints(currentVP + vpReward);
        accountRepository.save(account);

        // 6. Save vote record
        Vote vote = new Vote();
        vote.setUsername(username);
        vote.setVoteSite(voteSite);
        vote.setVotedAt(LocalDateTime.now());
        vote.setIpAddress(ipAddress);
        vote.setNxRewarded(nxReward);
        vote.setVotePointsRewarded(vpReward);
        voteRepository.save(vote);
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
}
