package com.tricolori.backend.core.domain.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

@Embeddable
@Data @NoArgsConstructor @AllArgsConstructor
public class Address {

    @Column(nullable = false)
    private String street;

    @Column(
            name = "street_num",
            nullable = false
    )
    private String streetNum;

    @Column(nullable = false)
    private String city;

    private String country = "Serbia";

    private Double longitude;

    private Double latitude;

}
