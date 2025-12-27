package com.cloudstory.backend.controller;

import com.cloudstory.backend.repository.AccountRepository;
import com.cloudstory.backend.service.OnlineUsersSimulation;
import com.cloudstory.backend.dto.OnlineCountResponse;
import com.cloudstory.backend.dto.ServerStatusResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/status")
public class StatusController {

    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private OnlineUsersSimulation onlineUsersSimulation;
    
    @Value("${game.expRate:1x}")
    private String expRate;
    
    @Value("${game.dropRate:2x}")
    private String dropRate;
    
    @Value("${game.mesoRate:1x}")
    private String mesoRate;

    @GetMapping
    public ResponseEntity<ServerStatusResponse> getStatus() {
        // Update real online count from database (users with loggedin > 0)
        long realOnlineCount = accountRepository.countByLoggedinGreaterThan(0);
        onlineUsersSimulation.updateRealOnlineCount((int) realOnlineCount);
        
        int displayCount = onlineUsersSimulation.getOnlineCount();
        int realCount = onlineUsersSimulation.getRealOnlineCount();
        boolean isSimulated = realCount < 10;
        
        return ResponseEntity.ok(new ServerStatusResponse(
            "Online",
            displayCount,
            realCount,
            isSimulated,
            expRate,
            dropRate,
            mesoRate
        ));
    }
    
    /**
     * Get online user count (shows real count from database)
     */
    @GetMapping("/online-count")
    public ResponseEntity<OnlineCountResponse> getOnlineCount() {
        // Update real online count from database (users with loggedin > 0)
        long realOnlineCount = accountRepository.countByLoggedinGreaterThan(0);
        onlineUsersSimulation.updateRealOnlineCount((int) realOnlineCount);
        
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


