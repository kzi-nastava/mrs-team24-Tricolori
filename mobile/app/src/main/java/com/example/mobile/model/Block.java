package com.example.mobile.model;

import java.time.LocalDateTime;

public class Block {
    public LocalDateTime blockedAt;
    public String blockReason;

    public Block(LocalDateTime blockedAt, String blockReason) {
        this.blockedAt = blockedAt;
        this.blockReason = blockReason;
    }
}
