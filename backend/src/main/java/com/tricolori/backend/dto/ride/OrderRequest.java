package com.tricolori.backend.dto.ride;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class OrderRequest {
    private RideRoute route;
    private RidePreferences preferences;
    private LocalDateTime createdAt;
    private String[] trackers;
}
