package com.tricolori.backend.mapper;

import com.tricolori.backend.dto.ride.DetailedRouteResponse;
import com.tricolori.backend.entity.Route;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RouteMapper {

    @Mapping(target = "pickupAddress", expression = "java(route.getStops().get(0).getAddress())")
    @Mapping(target = "pickupLatitude", expression = "java(route.getStops().get(0).getLocation().getLatitude())")
    @Mapping(target = "pickupLongitude", expression = "java(route.getStops().get(0).getLocation().getLongitude())")

    @Mapping(target = "destinationAddress",
            expression = "java(route.getStops().get(route.getStops().size()-1).getAddress())")
    @Mapping(target = "destinationLatitude",
            expression = "java(route.getStops().get(route.getStops().size()-1).getLocation().getLatitude())")
    @Mapping(target = "destinationLongitude",
            expression = "java(route.getStops().get(route.getStops().size()-1).getLocation().getLongitude())")

    @Mapping(target = "stops",
            expression = "java(route.getStops().size() > 2 ? route.getStops().subList(1, route.getStops().size()-1) : null)")

    @Mapping(target = "distanceKm", source = "distanceKm")
    @Mapping(target = "estimatedTimeSeconds",
            expression = "java(route.getEstimatedTimeSeconds() != null ? Math.toIntExact(route.getEstimatedTimeSeconds()) : null)")
    DetailedRouteResponse toDetailedRoute(Route route);
}

