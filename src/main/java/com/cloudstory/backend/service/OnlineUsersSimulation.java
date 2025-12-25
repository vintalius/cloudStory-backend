package com.cloudstory.backend.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.Random;

@Service
public class OnlineUsersSimulation {
    
    private int simulatedOnlineCount = 15; // Start with random 10-30
    private final Random random = new Random();
    private int realOnlineCount = 0; // Track actual logins
    
    public OnlineUsersSimulation() {
        // Initialize with random between 10-30
        this.simulatedOnlineCount = 10 + new Random().nextInt(21);
    }
    
    /**
     * Get the online count to display.
     * If real users >= 10, show real count. Otherwise show simulated.
     */
    public int getOnlineCount() {
        if (realOnlineCount >= 10) {
            return realOnlineCount;
        }
        return simulatedOnlineCount;
    }
    
    /**
     * Update real online count (when user logs in/out)
     */
    public void updateRealOnlineCount(int count) {
        this.realOnlineCount = count;
    }
    
    /**
     * Increment real online count (user login)
     */
    public void incrementRealOnlineCount() {
        this.realOnlineCount++;
    }
    
    /**
     * Decrement real online count (user logout)
     */
    public void decrementRealOnlineCount() {
        if (this.realOnlineCount > 0) {
            this.realOnlineCount--;
        }
    }
    
    /**
     * Get real online count (for admin/debug)
     */
    public int getRealOnlineCount() {
        return realOnlineCount;
    }
    
    /**
     * Run every 2-5 minutes to update simulated count
     * Increment/decrement by 1-2 randomly
     */
    @Scheduled(fixedDelay = 120000, initialDelay = 60000) // 2 min = 120 seconds
    public void updateSimulatedOnlineCount() {
        // Only update if we haven't hit 10 real users yet
        if (realOnlineCount < 10) {
            int change = random.nextBoolean() ? 
                (random.nextBoolean() ? 1 : 2) :  // +1 or +2
                -(random.nextBoolean() ? 1 : 2);   // -1 or -2
            
            simulatedOnlineCount += change;
            
            // Keep it between 10 and 30
            if (simulatedOnlineCount < 10) {
                simulatedOnlineCount = 10;
            } else if (simulatedOnlineCount > 30) {
                simulatedOnlineCount = 30;
            }
            
            System.out.println("[Simulation] Online count updated to: " + simulatedOnlineCount + 
                             " (Real: " + realOnlineCount + ")");
        }
    }
}
