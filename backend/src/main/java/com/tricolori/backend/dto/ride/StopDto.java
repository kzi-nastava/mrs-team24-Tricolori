package com.tricolori.backend.dto.ride;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StopDto {

    private String address;

    private Double latitude;

    private Double longitude;
}