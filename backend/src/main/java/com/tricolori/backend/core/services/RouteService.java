package com.tricolori.backend.core.services;

import com.tricolori.backend.core.domain.models.Location;
import com.tricolori.backend.core.domain.models.Route;
import com.tricolori.backend.core.domain.models.Stop;
import com.tricolori.backend.core.domain.repositories.RouteRepository;
import com.tricolori.backend.infrastructure.external.osrm.dto.OSRMRouteResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RouteService {

    private final RouteRepository routeRepository;
    private final OSRMService osrmService;

    // finds or crates a route based on stops, uses polyline as identifier
    @Transactional
    public Route findOrCreateRoute(List<Stop> stops) {
        if (stops == null || stops.size() < 2) {
            throw new IllegalArgumentException("Potrebna su minimum 2 stop-a (start i destination)");
        }

        List<Location> locations = stops.stream()
                .map(Stop::getLocation)
                .collect(Collectors.toList());

        OSRMRouteResponse osrmResponse = osrmService.getRoute(locations);
        OSRMRouteResponse.OSRMRoute osrmRoute = osrmResponse.getRoutes().get(0);
        String geometry = osrmRoute.getGeometry();

        Optional<Route> existingRoute = routeRepository.findByRouteGeometry(geometry);

        if (existingRoute.isPresent()) {
            log.info("Pronađena keširana ruta sa geometry: {}", geometry.substring(0, 20) + "...");
            return existingRoute.get();
        }

        log.info("Kreiram novu rutu sa geometry: {}", geometry.substring(0, 20) + "...");
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