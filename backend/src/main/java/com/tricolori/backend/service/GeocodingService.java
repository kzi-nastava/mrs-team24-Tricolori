package com.tricolori.backend.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
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
        String query = cyrilicToLatin(address);
        // String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String finalUrl = NOMINATIM_URL + query;

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Cuber/1.0 (mdujanovic03@gmail.com)");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<NominatimResponse[]> response = restTemplate.exchange(
                finalUrl,
                HttpMethod.GET,
                entity,
                NominatimResponse[].class
            );

            if (response == null || response.getBody() == null || response.getBody().length == 0) {
                throw new BadAddressException("Couldn't find lat and lng for address: " + address);
            }

            NominatimResponse bestChoice = response.getBody()[0];
            return bestChoice;
        } catch (Exception e) {
            System.err.println("Greška prilikom geokodiranja: " + e.getMessage());
        }
        throw new BadAddressException("Couldn't find lat and lng for address: " + address);
    }

    public String getAddressFromCoordinates(double lat, double lng) {
        String url = String.format("https://nominatim.openstreetmap.org/reverse?format=json&lat=%f&lon=%f", lat, lng);

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Cuber/1.0 (mdujanovic03@gmail.com)");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<com.fasterxml.jackson.databind.JsonNode> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    com.fasterxml.jackson.databind.JsonNode.class
            );

            if (response.getBody() != null && response.getBody().has("display_name")) {
                return response.getBody().get("display_name").asText();
            }
        } catch (Exception e) {
            System.err.println("Error while reverse geocoding: " + e.getMessage());
        }
        return "Non-existent location (" + lat + ", " + lng + ")";
    }

    public String cyrilicToLatin(String text) {
        String[] cirilica = {
            "\u0430", "\u0431", "\u0432", "\u0433", "\u0434", "\u0452", "\u0435", "\u0436", "\u0437", "\u0438", "\u0439", "\u043a", "\u043b", "\u0459", "\u043c",
            "\u043d", "\u045a", "\u043e", "\u043f", "\u0440", "\u0441", "\u0442", "\u045b", "\u0443", "\u0444", "\u0445", "\u0446", "\u0447", "\u045f", "\u0448",
            "\u0410", "\u0411", "\u0412", "\u0413", "\u0414", "\u0402", "\u0415", "\u0416", "\u0417", "\u0418", "\u0419", "\u041a", "\u041b", "\u0409", "\u041c",
            "\u041d", "\u040a", "\u041e", "\u041f", "\u0420", "\u0421", "\u0422", "\u040b", "\u0423", "\u0424", "\u0425", "\u0426", "\u0427", "\u040f", "\u0428"
        };
        
        String[] latinica = {
            "a", "b", "v", "g", "d", "dj", "e", "z", "z", "i", "j", "k", "l", "lj", "m",
            "n", "nj", "o", "p", "r", "s", "t", "c", "u", "f", "h", "c", "c", "dz", "s",
            "A", "B", "V", "G", "D", "Dj", "E", "Z", "Z", "I", "J", "K", "L", "Lj", "M",
            "N", "Nj", "O", "P", "R", "S", "T", "C", "U", "F", "H", "C", "C", "Dz", "S"
        };
        
        String result = text;
        for (int i = 0; i < cirilica.length; i++) {
            result = result.replace(cirilica[i], latinica[i]);
        }
        return result;
    }
}