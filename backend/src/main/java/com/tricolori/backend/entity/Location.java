package com.tricolori.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data @NoArgsConstructor @AllArgsConstructor
public class Location {

    @Column(nullable = true)
    private Double longitude = 0.0;

    @Column(nullable = true)
    private Double latitude = 0.0;

    public static double calculateDistance(Location l1, Location l2) {
        if (l1 == null || l2 == null) return Double.MAX_VALUE;
        return Math.sqrt(Math.pow(l1.getLatitude() - l2.getLatitude(), 2) + 
                         Math.pow(l1.getLongitude() - l2.getLongitude(), 2));
    }
}
