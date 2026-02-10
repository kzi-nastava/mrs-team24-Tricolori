package com.tricolori.backend.dto.route;

import com.tricolori.backend.dto.ride.RouteDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteRouteResponse {
    Long routeId;
    RouteDto route;
    String title;
}
