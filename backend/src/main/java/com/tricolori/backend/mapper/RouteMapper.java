package com.tricolori.backend.mapper;

import com.tricolori.backend.dto.ride.DetailedRouteResponse;
import com.tricolori.backend.entity.Route;
import com.tricolori.backend.entity.Stop;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RouteMapper {

    @Mapping(target = "pickupAddress", source = "stops", qualifiedByName = "pickupAddress")
    @Mapping(target = "pickupLatitude", source = "stops", qualifiedByName = "pickupLat")
    @Mapping(target = "pickupLongitude", source = "stops", qualifiedByName = "pickupLng")
    @Mapping(target = "destinationAddress", source = "stops", qualifiedByName = "destAddress")
    @Mapping(target = "destinationLatitude", source = "stops", qualifiedByName = "destLat")
    @Mapping(target = "destinationLongitude", source = "stops", qualifiedByName = "destLng")
    @Mapping(target = "stops", source = "stops", qualifiedByName = "intermediateStops")
    @Mapping(target = "estimatedTimeSeconds", expression = "java(route.getEstimatedTimeSeconds() != null ? Math.toIntExact(route.getEstimatedTimeSeconds()) : null)")
    DetailedRouteResponse toDetailedRoute(Route route);

    // ===== helpers =====

    @Named("pickupAddress")
    default String pickupAddress(List<Stop> stops) {
        return stops.get(0).getAddress();
    }

    @Named("pickupLat")
    default Double pickupLat(List<Stop> stops) {
        return stops.get(0).getLocation().getLatitude();
    }

    @Named("pickupLng")
    default Double pickupLng(List<Stop> stops) {
        return stops.get(0).getLocation().getLongitude();
    }

    @Named("destAddress")
    default String destAddress(List<Stop> stops) {
        return stops.get(stops.size() - 1).getAddress();
    }

    @Named("destLat")
    default Double destLat(List<Stop> stops) {
        return stops.get(stops.size() - 1).getLocation().getLatitude();
    }

    @Named("destLng")
    default Double destLng(List<Stop> stops) {
        return stops.get(stops.size() - 1).getLocation().getLongitude();
    }

    @Named("intermediateStops")
    default List<Stop> intermediateStops(List<Stop> stops) {
        return stops.size() > 2 ? stops.subList(1, stops.size() - 1) : null;
    }
}

