package com.tricolori.backend.service;

import com.tricolori.backend.infrastructure.presentation.dtos.Route.OSRMResult;

import com.tricolori.backend.entity.Location;
import com.tricolori.backend.entity.Route;
import com.tricolori.backend.entity.Stop;
import com.tricolori.backend.repository.RouteRepository;
import com.tricolori.backend.dto.osrm.OSRMRouteResponse;
import com.tricolori.backend.dto.ride.NominatimResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RouteService {

    private final RouteRepository routeRepository;
    private final OSRMService osrmService;
    private final GeocodingService geocodingService;

    public Route createRoute(Stop pickup, Stop destination, List<Stop> stops) {
        Route route = new Route();
        List<Stop> allStops = new ArrayList<>();
        // Save stops in order:
        allStops.add(pickup); allStops.addAll(stops); allStops.add(destination);
        allStops.forEach(this::locateStop);

        route.setStops(allStops);
        
        OSRMResult result = osrmService.analyzeRouteStops(allStops);
        route.setDistanceKm(result.getDistanceKilometers());
        route.setEstimatedTimeSeconds(result.getDurationSeconds());
        route.setRouteGeometry(result.getGeometry());

        return routeRepository.save(route);
    }

    private void locateStop(Stop stop) {
        Location location = stop.getLocation();
        if (location.getLatitude() != null && location.getLongitude() != null)
            return;

        NominatimResponse response = geocodingService.getAddressCoordinates(stop.getAddress());
        location.setLatitude(Double.parseDouble(response.getLat()));
        location.setLongitude(Double.parseDouble(response.getLon()));
        stop.setAddress(response.getDisplay_name());
    }

    // finds or crates a route based on stops, uses polyline as identifier
    @Transactional
    public Route findOrCreateRoute(List<Stop> stops) {
        if (stops == null || stops.size() < 2) {
            throw new IllegalArgumentException("At least two stops are needed - start and destination");
        }

        List<Location> locations = stops.stream()
                .map(Stop::getLocation)
                .collect(Collectors.toList());

        OSRMRouteResponse osrmResponse = osrmService.getRoute(locations);
        OSRMRouteResponse.OSRMRoute osrmRoute = osrmResponse.getRoutes().get(0);
        String geometry = osrmRoute.getGeometry();

        Optional<Route> existingRoute = routeRepository.findByRouteGeometry(geometry);

        if (existingRoute.isPresent()) {
            log.info("Found cached route with geometry: {}", geometry.substring(0, 20) + "...");
            return existingRoute.get();
        }

        log.info("Creating new route with geometry: {}", geometry.substring(0, 20) + "...");
        return createNewRoute(stops, osrmRoute);
    }

    private Route createNewRoute(List<Stop> stops, OSRMRouteResponse.OSRMRoute osrmRoute) {
        Route route = new Route();
        route.setStops(stops);
        route.setRouteGeometry(osrmRoute.getGeometry()); // encoded polyline
        route.setDistanceKm(osrmRoute.getDistance() / 1000.0); // iz metara u kilometre
        route.setEstimatedTimeSeconds(osrmRoute.getDuration().longValue());

        return routeRepository.save(route);
    }

    public Optional<Route> findById(Long id) {
        return routeRepository.findById(id);
    }
}