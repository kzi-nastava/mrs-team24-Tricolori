package com.tricolori.backend.core.domain.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "Vehicle")
@Table(name = "vehicles")
@Getter @Setter @NoArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "plate_num",
            unique = true,
            nullable = false
    )
    private String plateNum;

    @Column(nullable = false)
    private String model;

    private boolean available = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "specification_id",
            nullable = false
    )
    private VehicleSpecification specification;

    @Embedded
    private Location location;

}
