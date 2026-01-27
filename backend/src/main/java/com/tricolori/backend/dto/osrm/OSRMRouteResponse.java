package com.tricolori.backend.dto.osrm;

import lombok.Data;

import java.util.List;

@Data
public class OSRMRouteResponse {
    private String code;
    private List<OSRMRoute> routes;

    @Data
    public static class OSRMRoute {
        private Double distance; // in meters
        private Double duration; // in seconds
        private String geometry; // encoded polyline
    }
}