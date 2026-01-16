package com.cloudstory.backend.controller;

import com.cloudstory.backend.repository.AccountRepository;
import com.cloudstory.backend.service.OnlineUsersSimulation;
import com.cloudstory.backend.dto.OnlineCountResponse;
import com.cloudstory.backend.dto.ServerStatusResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.CacheControl;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/status")
public class StatusController {

    private static final Logger logger = LoggerFactory.getLogger(StatusController.class);

    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private OnlineUsersSimulation onlineUsersSimulation;
    
    @Value("${game.expRate:1x}")
    private String expRate;
    
    @Value("${game.dropRate:1x}")
    private String dropRate;
    
    @Value("${game.mesoRate:1x}")
    private String mesoRate;

    /**
     * Helper method to fetch and cache online count data - eliminates duplicate DB queries
     */
    private void updateOnlineCountData() {
        long realOnlineCount = accountRepository.countByLoggedinGreaterThan(0);
        onlineUsersSimulation.updateRealOnlineCount((int) realOnlineCount);
    }

    @GetMapping
    public ResponseEntity<ServerStatusResponse> getStatus() {
        logger.info("📊 /api/status endpoint called - dropRate: {}, expRate: {}, mesoRate: {}", dropRate, expRate, mesoRate);

        updateOnlineCountData();

        int displayCount = onlineUsersSimulation.getOnlineCount();
        int realCount = onlineUsersSimulation.getRealOnlineCount();
        boolean isSimulated = realCount < 10;
        
        ServerStatusResponse response = new ServerStatusResponse(
            "Online",
            displayCount,
            realCount,
            isSimulated,
            expRate,
            dropRate,
            mesoRate
        );

        // Add cache control headers - cache for 10 seconds
        // This reduces duplicate requests from frontend
        return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(10, TimeUnit.SECONDS).cachePublic())
            .body(response);
    }
    
    /**
     * Get online user count (shows real count from database)
     */
    @GetMapping("/online-count")
    public ResponseEntity<OnlineCountResponse> getOnlineCount() {
        logger.info("📊 /api/status/online-count endpoint called");

        updateOnlineCountData();

        int displayCount = onlineUsersSimulation.getOnlineCount();
        int realCount = onlineUsersSimulation.getRealOnlineCount();
        boolean isSimulated = realCount < 10;
        
        return ResponseEntity.ok(new OnlineCountResponse(
            displayCount,
            realCount,
            isSimulated
        ));
    }
}


