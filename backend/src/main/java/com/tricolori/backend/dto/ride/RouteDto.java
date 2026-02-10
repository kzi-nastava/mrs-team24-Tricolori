package com.tricolori.backend.dto.ride;

import java.util.List;

import com.tricolori.backend.entity.Stop;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteDto {
    private Stop pickupStop;
    private Stop destinationStop;
    private List<Stop> stops;     // order in list determines the order in stops
}
