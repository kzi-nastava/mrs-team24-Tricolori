package com.tricolori.backend.core.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.tricolori.backend.core.domain.models.Route;
import com.tricolori.backend.core.domain.models.Stop;
import com.tricolori.backend.core.domain.repositories.RouteRepository;
import com.tricolori.backend.core.exceptions.NoRouteGeometryException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RouteService {
    private final RouteRepository repository;
    private final RestTemplate restTemplate;

    public static final String OSRM_PUBLIC_URL = "https://router.project-osrm.org/route/v1/driving/";

    // All I need for route are stops...
    public Route createRoute(Stop pickup, Stop destination, List<Stop> stops) {
        Route route = new Route();
        List<Stop> allStops = new ArrayList<>();
        allStops.add(pickup); allStops.addAll(stops); allStops.add(destination);
        route.setStops(allStops);
        
        // Connecting stops into string format: lon,lat;lon,lat
        StringBuilder coordsBuilder = new StringBuilder();
        coordsBuilder.append(pickup.toCoordinates());
        for (Stop stop : stops) {
            coordsBuilder.append(stop.toCoordinates());
            coordsBuilder.append(';');
        }
        coordsBuilder.append(destination.toCoordinates());

        String finalUrl = OSRM_PUBLIC_URL + coordsBuilder.toString() + "?overview=full&geometries=polyline";

        JsonNode response = restTemplate.getForObject(finalUrl, JsonNode.class);        
        if (response == null || !response.has("routes")) {
            throw new NoRouteGeometryException();
        }
        
        JsonNode bestRoute = response.get("routes").get(0);

        // 3. Setovanje podataka u tvoj model
        route.setDistanceKm(bestRoute.get("distance").asDouble() / 1000.0);
        route.setEstimatedTimeSeconds(bestRoute.get("duration").asLong());
        route.setRouteGeometry(bestRoute.get("geometry").asText());
        
        return repository.save(route);
    }
}
