package com.tricolori.backend.core.domain.models;

import com.tricolori.backend.shared.enums.VehicleType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "VehicleSpecification")
@Table(name = "vehicle_specifications")
@Getter @Setter @NoArgsConstructor
public class VehicleSpecification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private VehicleType type;

    @Min(value = 1) @Max(value = 10)
    @Column(name = "num_seats")
    private int numSeats;

    @Column(name = "baby_friendly")
    private boolean babyFriendly;

    @Column(name = "pet_friendly")
    private boolean petFriendly;

}
