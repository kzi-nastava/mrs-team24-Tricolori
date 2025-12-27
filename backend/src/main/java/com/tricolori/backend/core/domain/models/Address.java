package com.tricolori.backend.core.domain.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data @NoArgsConstructor @AllArgsConstructor
public class Address {

    @Column(nullable = false)
    private String address;

    private String city;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private Double latitude;
}