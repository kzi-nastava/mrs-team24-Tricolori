package com.tricolori.backend.mapper;

import java.util.ArrayList;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.tricolori.backend.dto.ride.RouteDto;
import com.tricolori.backend.dto.route.FavoriteRouteResponse;
import com.tricolori.backend.entity.FavoriteRoute;
import com.tricolori.backend.entity.Route;
import com.tricolori.backend.entity.Stop;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FavoriteRouteMapper {
    @Mapping(source = "route.id", target = "routeId")
    @Mapping(source = "route", target = "route")
    @Mapping(source = "title", target = "title")
    FavoriteRouteResponse toResponse(FavoriteRoute favoriteRoute);

    List<FavoriteRouteResponse> toResponseList(List<FavoriteRoute> list);

    default RouteDto mapRouteToDto(Route route) {
        if (route == null || route.getStops() == null || route.getStops().isEmpty()) {
            return null;
        }
        
        List<Stop> allStops = route.getStops();
        Stop pickup = allStops.get(0);
        Stop destination = allStops.get(allStops.size() - 1);
        
        List<Stop> intermediate = new ArrayList<>();
        if (allStops.size() > 2) {
            intermediate.addAll(allStops.subList(1, allStops.size() - 1));
        }

        return new RouteDto(pickup, destination, intermediate);
    }
}
