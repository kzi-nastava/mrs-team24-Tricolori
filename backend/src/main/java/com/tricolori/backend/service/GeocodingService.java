package com.tricolori.backend.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.tricolori.backend.dto.ride.NominatimResponse;
import com.tricolori.backend.exception.BadAddressException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GeocodingService {
    private final RestTemplate restTemplate;

    private final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search?format=json&q=";

    public NominatimResponse getAddressCoordinates(String address) {
        // Add more precision...
        // String query = address + ", Novi Sad, Serbia";
        String query = address;
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Cuber/1.0 (mdujanovic03@gmail.com)");
        HttpEntity<String> entity = new HttpEntity<String>(headers);

        try {
            ResponseEntity<NominatimResponse[]> response = restTemplate.exchange(
                NOMINATIM_URL + encodedQuery,
                HttpMethod.GET,
                entity,
                NominatimResponse[].class
            );

            if (response.getBody() != null && response.getBody().length > 0) {
                return response.getBody()[0];
            }
        } catch (Exception e) {
            System.err.println("Greška prilikom geokodiranja: " + e.getMessage());
        }

        throw new BadAddressException("Couldn't find lat and lng for address: " + address);
    }
}