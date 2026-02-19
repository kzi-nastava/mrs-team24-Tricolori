package com.example.mobile.dto.block;

public class BlockRequest {
    private String blockReason;
    private String userEmail;

    public BlockRequest(String blockReason, String userEmail) {
        this.blockReason = blockReason;
        this.userEmail = userEmail;
    }

    public String getBlockReason() { return blockReason; }
    public void setBlockReason(String blockReason) { this.blockReason = blockReason; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
}
