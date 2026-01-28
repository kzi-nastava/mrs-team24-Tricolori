package com.tricolori.backend.dto.ride;

import java.util.ArrayList;
import java.util.List;

import com.tricolori.backend.entity.Stop;

public record RideRoute(
    Stop pickup,
    Stop destination,
    List<Stop> stops
) 
{
    public List<Stop> getAllStops() {
        List<Stop> all = new ArrayList<>();
        all.add(pickup);
        if (stops != null) {
            all.addAll(stops);
        }
        all.add(destination);
        return all;
    }
}
