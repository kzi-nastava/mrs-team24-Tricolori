package com.tricolori.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data @NoArgsConstructor @AllArgsConstructor
public class Stop {

    @Column(nullable = false)
    private String address;

    @Embedded
    private Location location;

    public String toCoordinates() {
        return location.getLongitude().toString() + "," + location.getLatitude().toString();
    }
}