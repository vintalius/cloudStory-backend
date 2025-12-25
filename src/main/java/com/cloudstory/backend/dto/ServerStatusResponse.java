package com.cloudstory.backend.dto;

public class ServerStatusResponse {
    private String status;
    private int onlineCount;
    private int realOnlineCount;
    private boolean isSimulated;
    private String expRate;
    private String dropRate;
    private String mesoRate;

    public ServerStatusResponse(String status, int onlineCount, int realOnlineCount, boolean isSimulated, String expRate, String dropRate, String mesoRate) {
        this.status = status;
        this.onlineCount = onlineCount;
        this.realOnlineCount = realOnlineCount;
        this.isSimulated = isSimulated;
        this.expRate = expRate;
        this.dropRate = dropRate;
        this.mesoRate = mesoRate;
    }

    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getOnlineCount() { return onlineCount; }
    public void setOnlineCount(int onlineCount) { this.onlineCount = onlineCount; }

    public int getRealOnlineCount() { return realOnlineCount; }
    public void setRealOnlineCount(int realOnlineCount) { this.realOnlineCount = realOnlineCount; }

    public boolean isSimulated() { return isSimulated; }
    public void setSimulated(boolean simulated) { isSimulated = simulated; }

    public String getExpRate() { return expRate; }
    public void setExpRate(String expRate) { this.expRate = expRate; }

    public String getDropRate() { return dropRate; }
    public void setDropRate(String dropRate) { this.dropRate = dropRate; }

    public String getMesoRate() { return mesoRate; }
    public void setMesoRate(String mesoRate) { this.mesoRate = mesoRate; }
}
