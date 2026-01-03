package com.cloudstory.backend.controller;

import com.cloudstory.backend.dto.VoteStatusDTO;
import com.cloudstory.backend.dto.VoteTierDTO;
import com.cloudstory.backend.entity.Vote;
import com.cloudstory.backend.service.VoteService;
import com.cloudstory.backend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vote")
public class VoteController {

    @Autowired
    private VoteService voteService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Get vote status for all voting sites
     * Used by frontend to display cooldown timers
     * 
     * @param authHeader Authorization header with JWT token
     * @return Map of voting sites with their status
     */
    @GetMapping("/status")
    public ResponseEntity<?> getVoteStatus(@RequestHeader("Authorization") String authHeader) {
        try {
            String username = getUsernameFromToken(authHeader);

            Map<String, VoteStatusDTO> statuses = new HashMap<>();
            String[] sites = {"gtop100", "topg", "xtremetop100", "arena-top100"};

            for (String site : sites) {
                statuses.put(site, voteService.getVoteStatus(username, site));
            }

            return ResponseEntity.ok(statuses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Callback endpoint for voting sites
     * Supports multiple formats:
     * 
     * GTOP100 (POST/JSON):
     *   - pb_name: Username from pingback
     *   - pingbackkey: Secret key
     *   - ip: Voter IP address
     * 
     * TopG (Query Parameters):
     *   - p_resp: Username (from URL parameter in vote link)
     *   - ip: Voter IP
     *   - key: Secret key
     * 
     * XtremeTop100 (Query Parameters):
     *   - custom: Username (from postback parameter in vote link)
     *   - votingip: Voter IP
     *   - key: Secret key (in Authorization header or from config)
     * 
     * Arena-Top100 (Query Parameters):
     *   - username: Player username
     *   - key: Secret key
     * 
     * @param site The voting site name (gtop100, topg, xtremetop100, arena-top100)
     * @param username Optional: username from query parameters
     * @param key Optional: secret key from query parameters
     * @param pb_name Optional: GTOP100 username
     * @param pingbackkey Optional: GTOP100 secret key
     * @param p_resp Optional: TopG username
     * @param custom Optional: XtremeTop100 username
     * @param votingip Optional: XtremeTop100 voter IP
     * @param request HTTP request to get IP address and/or POST body
     * @return Success or error message
     */
    @PostMapping("/callback/{site}")
    public ResponseEntity<?> voteCallback(
            @PathVariable String site,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String key,
            @RequestParam(required = false) String pb_name,
            @RequestParam(required = false) String pingbackkey,
            @RequestParam(required = false) String p_resp,
            @RequestParam(required = false) String ip,
            @RequestParam(required = false) String custom,
            @RequestParam(required = false) String votingip,
            @RequestParam(required = false) String secret,
            @RequestParam(required = false) String voted,
            HttpServletRequest request) {
        
        try {
            // Determine username and secret based on voting site
            String finalUsername = username;
            String finalKey = key;
            String finalIp = ip;
            boolean voteSuccess = true;
            
            // Handle GTOP100 (uses pb_name and pingbackkey)
            if ("gtop100".equals(site)) {
                finalUsername = pb_name;
                finalKey = pingbackkey;
            }
            // Handle TopG (uses p_resp and ip)
            else if ("topg".equals(site)) {
                finalUsername = p_resp;
                // TopG doesn't send key in callback
            }
            // Handle XtremeTop100 (uses custom and votingip)
            else if ("xtremetop100".equals(site)) {
                finalUsername = custom;
                finalIp = votingip;
            }
            // Handle Arena-Top100 (uses username, secret, and voted flag)
            else if ("arena-top100".equals(site)) {
                finalKey = secret;
                // Check if vote was successful (1=success, 0=failed)
                voteSuccess = "1".equals(voted);
            }
            
            // Validate required parameters
            if (finalUsername == null || finalUsername.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Username not provided"
                ));
            }
            
            // For sites that require key validation
            if (("gtop100".equals(site) || "arena-top100".equals(site))) {
                if (finalKey == null || finalKey.isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "Secret key not provided"
                    ));
                }
            }
            
            // Use provided IP or get from request
            if (finalIp == null || finalIp.isEmpty()) {
                finalIp = getClientIpAddress(request);
            }
            
            voteService.processVote(finalUsername, site, finalKey, finalIp, voteSuccess);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Vote recorded successfully",
                "username", finalUsername,
                "site", site
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Get voting history for the authenticated user
     * 
     * @param authHeader Authorization header with JWT token
     * @return List of all votes by the user
     */
    @GetMapping("/history")
    public ResponseEntity<?> getVoteHistory(@RequestHeader("Authorization") String authHeader) {
        try {
            String username = getUsernameFromToken(authHeader);
            List<Vote> history = voteService.getUserVoteHistory(username);
            
            return ResponseEntity.ok(Map.of(
                "votes", history,
                "totalVotes", voteService.getUserVoteCount(username)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get total vote count for authenticated user
     * 
     * @param authHeader Authorization header with JWT token
     * @return Total number of votes
     */
    @GetMapping("/count")
    public ResponseEntity<?> getVoteCount(@RequestHeader("Authorization") String authHeader) {
        try {
            String username = getUsernameFromToken(authHeader);
            long count = voteService.getUserVoteCount(username);
            
            return ResponseEntity.ok(Map.of("totalVotes", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

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

    /**
     * Extract username from JWT token in Authorization header
     * 
     * @param authHeader Authorization header (format: "Bearer token")
     * @return Username from token
     */
    private String getUsernameFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid authorization header");
        }
        
        String token = authHeader.substring(7);
        return jwtUtil.extractUsername(token);
    }

    /**
     * Get client IP address from request
     * Handles proxies and load balancers
     * 
     * @param request HTTP request
     * @return Client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
