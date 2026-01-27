package com.tricolori.backend.core.services;

import com.tricolori.backend.core.domain.models.Location;
import com.tricolori.backend.infrastructure.external.osrm.dto.OSRMRouteResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OSRMService {

    private final RestTemplate restTemplate;
    // TODO: change base url
    private static final String OSRM_BASE_URL = "http://router.project-osrm.org";

    // accepts a list of at least 2 locations and returns geometry
    public OSRMRouteResponse getRoute(List<Location> locations) {
        if (locations == null || locations.size() < 2) {
            throw new IllegalArgumentException("Potrebne su minimum 2 lokacije (start i destination)");
        }

        // Format: longitude,latitude;longitude,latitude;...
        String coordinates = locations.stream()
                .map(loc -> loc.getLongitude() + "," + loc.getLatitude())
                .collect(Collectors.joining(";"));

        String url = UriComponentsBuilder
                .fromHttpUrl(OSRM_BASE_URL + "/route/v1/driving/" + coordinates)
                .queryParam("overview", "full") // full polyline
                .queryParam("geometries", "polyline") // encoded polyline format
                .toUriString();

        log.info("Pozivam OSRM: {}", url);

        OSRMRouteResponse response = restTemplate.getForObject(url, OSRMRouteResponse.class);

        if (response == null || !"Ok".equals(response.getCode())) {
            throw new RuntimeException("OSRM nije vratio validnu rutu");
        }

        return response;
    }

    // generates temporary polyline, used to check if the route exists
    public String generateTemporaryGeometry(List<Location> locations) {
        // Pozovi OSRM samo da dobiješ geometry
        OSRMRouteResponse response = getRoute(locations);
        return response.getRoutes().get(0).getGeometry();
    }
}