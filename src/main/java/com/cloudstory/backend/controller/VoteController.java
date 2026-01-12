package com.cloudstory.backend.controller;

import com.cloudstory.backend.dto.PendingNxDTO;
import com.cloudstory.backend.dto.VoteStatusDTO;
import com.cloudstory.backend.dto.VoteTierDTO;
import com.cloudstory.backend.entity.Vote;
import com.cloudstory.backend.service.PendingNxService;
import com.cloudstory.backend.service.VoteService;
import com.cloudstory.backend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vote")
public class VoteController {

    private static final Logger logger = LoggerFactory.getLogger(VoteController.class);

    @Autowired
    private VoteService voteService;

    @Autowired
    private PendingNxService pendingNxService;

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
     * Supports multiple formats and both GET/POST methods:
     * 
     * GTOP100 (POST/JSON):
     *   - pb_name: Username from pingback
     *   - pingbackkey: Secret key
     *   - ip: Voter IP address
     * 
     * TopG (GET):
     *   - p_resp: Username (from URL parameter in vote link)
     *   - ip: Voter IP
     *   - key: Secret key
     * 
     * XtremeTop100 (GET):
     *   - custom: Username (from postback parameter in vote link)
     *   - votingip: Voter IP
     *   - key: Secret key (in Authorization header or from config)
     * 
     * Arena-Top100 (GET/POST):
     *   - userid: Player username (sent as id= or postback= or incentive=)
     *   - secret: Secret key
     *   - voted: "1" for success, "0" for failure
     *   - userip: Voter IP
     * 
     * @param site The voting site name (gtop100, topg, xtremetop100, arena-top100)
     * @param username Optional: username from query parameters
     * @param userid Optional: Arena-Top100 username parameter
     * @param key Optional: secret key from query parameters
     * @param pb_name Optional: GTOP100 username
     * @param pingbackkey Optional: GTOP100 secret key
     * @param p_resp Optional: TopG username
     * @param custom Optional: XtremeTop100 username
     * @param votingip Optional: XtremeTop100 voter IP
     * @param userip Optional: Arena-Top100 voter IP
     * @param secret Optional: Arena-Top100 secret key
     * @param voted Optional: Arena-Top100 voted flag (1=success, 0=failure)
     * @param request HTTP request to get IP address and/or POST body
     * @return Success or error message
     */
    @RequestMapping(path = "/callback/{site}", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<?> voteCallback(
            @PathVariable String site,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String userid,
            @RequestParam(required = false) String key,
            @RequestParam(required = false) String pb_name,
            @RequestParam(required = false) String pingbackkey,
            @RequestParam(required = false) String p_resp,
            @RequestParam(required = false) String ip,
            @RequestParam(required = false) String custom,
            @RequestParam(required = false) String votingip,
            @RequestParam(required = false) String userip,
            @RequestParam(required = false) String secret,
            @RequestParam(required = false) String voted,
            @RequestParam(required = false) String pingUsername,
            @RequestParam(required = false) String VoterIP,
            HttpServletRequest request) {
        
        try {
            // Sanitize parameters (handle comma-separated duplicates)
            username = sanitizeParam(username);
            userid = sanitizeParam(userid);
            key = sanitizeParam(key);
            pb_name = sanitizeParam(pb_name);
            pingbackkey = sanitizeParam(pingbackkey);
            pingUsername = sanitizeParam(pingUsername);
            VoterIP = sanitizeParam(VoterIP);
            p_resp = sanitizeParam(p_resp);
            ip = sanitizeParam(ip);
            custom = sanitizeParam(custom);
            votingip = sanitizeParam(votingip);
            userip = sanitizeParam(userip);
            secret = sanitizeParam(secret);
            voted = sanitizeParam(voted);

            logger.info("=== VOTE CALLBACK START ===");
            logger.info("Site: {}", site);
            logger.info("Parameters - username: {}, userid: {}, key: {}, pb_name: {}, pingbackkey: {}, p_resp: {}, ip: {}, custom: {}, votingip: {}, userip: {}, secret: {}, voted: {}", 
                username, userid, key, pb_name, pingbackkey, p_resp, ip, custom, votingip, userip, secret, voted);
            
            // Debug logging for Arena-Top100
            if ("arena-top100".equals(site)) {
                logger.warn("ARENA DEBUG: userid={}, secret={}, voted={}, userip={}", userid, secret, voted, userip);
            }
            
            // Determine username and secret based on voting site
            String finalUsername = username;
            String finalKey = key;
            String finalIp = ip;
            boolean voteSuccess = true;
            
            // Handle GTOP100 (uses pb_name and pingbackkey, or username and key, or pingUsername and VoterIP)
            if ("gtop100".equals(site)) {
                if (pb_name != null && !pb_name.isEmpty()) {
                    finalUsername = pb_name;
                } else if (pingUsername != null && !pingUsername.isEmpty()) {
                    finalUsername = pingUsername;
                }
                
                if (pingbackkey != null && !pingbackkey.isEmpty()) {
                    finalKey = pingbackkey;
                }
                
                if (VoterIP != null && !VoterIP.isEmpty()) {
                    finalIp = VoterIP;
                }
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
            // Handle Arena-Top100 (uses userid, secret, userip, and voted flag)
            else if ("arena-top100".equals(site)) {
                finalUsername = userid; // Arena-Top100 sends userid (from id= or postback= or incentive=)
                finalKey = secret;
                finalIp = userip;
                // Check if vote was successful (1=success, 0=failed)
                voteSuccess = "1".equals(voted);
            }
            
            logger.info("Processed - finalUsername: {}, finalKey: {}, finalIp: {}, voteSuccess: {}", finalUsername, finalKey, finalIp, voteSuccess);
            
            // Validate required parameters
            if (finalUsername == null || finalUsername.isEmpty()) {
                logger.error("Username not provided for site: {}", site);
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Username not provided"
                ));
            }
            
            // For sites that require key validation
            if (("gtop100".equals(site) || "arena-top100".equals(site))) {
                if (finalKey == null || finalKey.isEmpty()) {
                    logger.error("Secret key not provided for site: {}", site);
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
            
            logger.info("Calling processVote with username: {}, site: {}, ip: {}, voteSuccess: {}", finalUsername, site, finalIp, voteSuccess);
            voteService.processVote(finalUsername, site, finalKey, finalIp, voteSuccess);
            
            logger.info("Vote processed successfully");
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Vote recorded successfully",
                "username", finalUsername,
                "site", site
            ));
        } catch (Exception e) {
            logger.error("Error processing vote callback", e);
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

    /**
     * Dedicated endpoint for GTOP100 POST requests
     * Supports two formats:
     * 1. JSON batch format (recommended): Contains "Common" array with up to 50 votes
     *    - Each vote has: pb_name, ip, success (0=success, 1=fail), reason
     *    - pingbackkey is at root level
     * 2. POST form data: Single vote with pingUsername, VoterIP, Successful, pingbackkey
     */
    @PostMapping("/callback/gtop100")
    public ResponseEntity<?> gtop100Callback(@RequestBody(required = false) Map<String, Object> body, 
                                              @RequestParam(required = false) String pingUsername,
                                              @RequestParam(required = false) String VoterIP,
                                              @RequestParam(required = false) String Successful,
                                              @RequestParam(required = false) String pingbackkey,
                                              HttpServletRequest request) {
        try {
            logger.info("=== GTOP100 CALLBACK START ===");
            logger.info("Body: {}", body);
            logger.info("Params - pingUsername: {}, VoterIP: {}, Successful: {}, pingbackkey: {}", 
                pingUsername, VoterIP, Successful, pingbackkey);
            
            // Check if this is JSON batch format
            if (body != null && body.containsKey("Common")) {
                return handleGtop100JsonBatch(body, request);
            }
            // Handle POST form data format
            else if (pingUsername != null || VoterIP != null) {
                return handleGtop100PostForm(pingUsername, VoterIP, Successful, pingbackkey, request);
            }
            // Try to extract from JSON body (fallback for non-batch JSON)
            else if (body != null) {
                String user = (String) body.get("pingUsername");
                String ip = (String) body.get("VoterIP");
                String success = (String) body.get("Successful");
                String key = (String) body.get("pingbackkey");
                return handleGtop100PostForm(user, ip, success, key, request);
            }
            
            logger.error("GTOP100: No valid data format received");
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Invalid request format"
            ));
            
        } catch (Exception e) {
            logger.error("Error processing GTOP100 callback", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Handle GTOP100 JSON batch format (recommended by GTOP100)
     * Structure: { "siteid": 12345, "pingbackkey": "...", "Common": [[{pb_id:1},{ip:"..."},{success:0},{pb_name:"Player1"}],...] }
     */
    private ResponseEntity<?> handleGtop100JsonBatch(Map<String, Object> body, HttpServletRequest request) {
        logger.info("Processing GTOP100 JSON batch format");
        
        String pingbackkey = (String) body.get("pingbackkey");
        if (pingbackkey == null || pingbackkey.isEmpty()) {
            logger.error("GTOP100 JSON: Missing pingbackkey");
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Missing pingbackkey"));
        }
        
        @SuppressWarnings("unchecked")
        List<List<Map<String, Object>>> commonArray = (List<List<Map<String, Object>>>) body.get("Common");
        
        if (commonArray == null || commonArray.isEmpty()) {
            logger.error("GTOP100 JSON: Empty Common array");
            return ResponseEntity.ok(Map.of("success", true, "message", "No votes to process"));
        }
        
        int processed = 0;
        int failed = 0;
        
        for (List<Map<String, Object>> voteEntry : commonArray) {
            try {
                // Flatten the nested structure: [[{pb_id:1},{ip:"x"},{success:0},{pb_name:"y"}]]
                Map<String, Object> flattenedVote = new HashMap<>();
                for (Map<String, Object> field : voteEntry) {
                    flattenedVote.putAll(field);
                }
                
                String pb_name = (String) flattenedVote.get("pb_name");
                String ip = (String) flattenedVote.get("ip");
                Integer success = (Integer) flattenedVote.get("success"); // 0 = success, 1 = failed
                
                logger.info("GTOP100 Batch Vote - pb_name: {}, ip: {}, success: {}", pb_name, ip, success);
                
                if (pb_name == null || pb_name.isEmpty()) {
                    logger.warn("GTOP100: Skipping vote - no pb_name");
                    failed++;
                    continue;
                }
                
                boolean voteSuccess = (success != null && success == 0);
                String finalIp = (ip != null && !ip.isEmpty()) ? ip : getClientIpAddress(request);
                
                voteService.processVote(pb_name, "gtop100", pingbackkey, finalIp, voteSuccess);
                processed++;
                
            } catch (Exception e) {
                logger.error("Error processing GTOP100 batch vote", e);
                failed++;
            }
        }
        
        logger.info("GTOP100 Batch processed: {} successful, {} failed", processed, failed);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "processed", processed,
            "failed", failed
        ));
    }
    
    /**
     * Handle GTOP100 POST form data format
     * Parameters: pingUsername, VoterIP, Successful (0=success), pingbackkey
     */
    private ResponseEntity<?> handleGtop100PostForm(String pingUsername, String voterIP, 
                                                     String successful, String pingbackkey,
                                                     HttpServletRequest request) {
        logger.info("Processing GTOP100 POST form - pingUsername: {}, voterIP: {}, successful: {}", 
            pingUsername, voterIP, successful);
        
        if (pingUsername == null || pingUsername.isEmpty()) {
            logger.error("GTOP100 POST: Missing pingUsername");
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Missing username"));
        }
        
        if (pingbackkey == null || pingbackkey.isEmpty()) {
            logger.error("GTOP100 POST: Missing pingbackkey");
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Missing pingbackkey"));
        }
        
        boolean voteSuccess = "0".equals(successful); // 0 = success, 1 = failed
        String finalIp = (voterIP != null && !voterIP.isEmpty()) ? voterIP : getClientIpAddress(request);
        
        voteService.processVote(pingUsername, "gtop100", pingbackkey, finalIp, voteSuccess);
        
        logger.info("GTOP100 POST vote processed successfully");
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Vote recorded successfully",
            "username", pingUsername
        ));
    }

    private String sanitizeParam(String param) {
        if (param != null && param.contains(",")) {
            return param.split(",")[0].trim();
        }
        return param;
    }

    /**
     * Get pending NX rewards for the authenticated user
     * These are rewards waiting to be applied on next game login
     * 
     * @param authHeader Authorization header with JWT token
     * @return List of pending rewards
     */
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingRewards(@RequestHeader("Authorization") String authHeader) {
        try {
            String username = getUsernameFromToken(authHeader);
            List<PendingNxDTO> pendingRewards = pendingNxService.getPendingRewards(username);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "pending_rewards", pendingRewards,
                "message", pendingRewards.isEmpty() 
                    ? "No pending rewards" 
                    : "You have " + pendingRewards.size() + " pending reward(s). Login to the game to receive them!"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
