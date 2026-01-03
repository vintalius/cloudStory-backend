package com.cloudstory.backend.controller;

import com.cloudstory.backend.dto.VoteStatusDTO;
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
     * Voting sites call this URL after a user votes
     * 
     * Example URL: POST /api/vote/callback/gtop100?username=PlayerName&key=secret123
     * 
     * @param site The voting site name (gtop100, topg, etc.)
     * @param username The username who voted
     * @param key The secret key from voting site
     * @param request HTTP request to get IP address
     * @return Success or error message
     */
    @PostMapping("/callback/{site}")
    public ResponseEntity<?> voteCallback(
            @PathVariable String site,
            @RequestParam String username,
            @RequestParam String key,
            HttpServletRequest request) {
        
        try {
            String ipAddress = getClientIpAddress(request);
            voteService.processVote(username, site, key, ipAddress);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Vote recorded successfully",
                "username", username,
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
