package com.cloudstory.backend.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class OnlineUsersSimulation {
    
    private int realOnlineCount = 0; // Track actual logins
    
    public OnlineUsersSimulation() {
        // Initialize with 0 real online count
        this.realOnlineCount = 0;
    }
    
    /**
     * Get the online count to display.
     * Show real online count (no simulation/fake data)
     */
    public int getOnlineCount() {
        return realOnlineCount;
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
}

