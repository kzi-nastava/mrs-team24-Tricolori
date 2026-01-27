package com.tricolori.backend.core.domain.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity(name = "Route")
@Table(name = "routes")
@Getter @Setter @NoArgsConstructor
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Size(min = 2)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "route_stops",
            joinColumns = @JoinColumn(name = "route_id")
    )
    @OrderColumn(name = "stop_index")
    private List<Stop> stops = new ArrayList<>();

    @Column(name = "route_geometry", columnDefinition = "TEXT", unique = true)
    private String routeGeometry;

    @Column(name = "distance_km")
    private Double distanceKm;

    @Column(name = "estimated_time_seconds")
    private Long estimatedTimeSeconds;

    public Stop getPickupStop() {
        if (stops.isEmpty()) return null;
        return stops.getFirst();
    }

    public Stop getDestinationStop() {
        if (stops.isEmpty()) return null;
        return stops.getLast();
    }

}
