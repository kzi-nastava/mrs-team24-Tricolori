package com.tricolori.backend.dto.ride;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) 
public class NominatimResponse {
    private String lat;
    private String lon;
    private String display_name;
}
