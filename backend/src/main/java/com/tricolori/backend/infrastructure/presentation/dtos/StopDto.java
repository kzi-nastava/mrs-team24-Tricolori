package com.tricolori.backend.infrastructure.presentation.dtos;

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