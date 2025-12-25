package com.cloudstory.backend.dto;

public class OnlineCountResponse {
    private int onlineCount;
    private int realOnlineCount;
    private boolean isSimulated;
    private String message;

    public OnlineCountResponse(int onlineCount, int realOnlineCount, boolean isSimulated) {
        this.onlineCount = onlineCount;
        this.realOnlineCount = realOnlineCount;
        this.isSimulated = isSimulated;
        this.message = isSimulated ? 
            "Showing simulated count (will switch to real at 10+ users)" : 
            "Showing real online count";
    }

    // Getters and Setters
    public int getOnlineCount() { return onlineCount; }
    public void setOnlineCount(int onlineCount) { this.onlineCount = onlineCount; }

    public int getRealOnlineCount() { return realOnlineCount; }
    public void setRealOnlineCount(int realOnlineCount) { this.realOnlineCount = realOnlineCount; }

    public boolean isSimulated() { return isSimulated; }
    public void setSimulated(boolean simulated) { isSimulated = simulated; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
