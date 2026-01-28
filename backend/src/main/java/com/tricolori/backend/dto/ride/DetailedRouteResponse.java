package com.tricolori.backend.dto.ride;

import com.tricolori.backend.entity.Stop;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetailedRouteResponse {
    private Long id;
    private String pickupAddress;
    private Double pickupLatitude;
    private Double pickupLongitude;
    private String destinationAddress;
    private Double destinationLatitude;
    private Double destinationLongitude;
    private List<Stop> stops;              // Intermediate stops only
    private Double distanceKm;
    private Integer estimatedTimeSeconds;
}