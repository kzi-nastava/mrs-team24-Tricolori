package com.tricolori.backend.core.domain.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity(name = "Review")
@Table(name = "reviews")
@Getter @Setter @NoArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_rating", nullable = false)
    @Min(1)
    @Max(5)
    private Integer vehicleRating;

    @Column(name = "driver_rating", nullable = false)
    @Min(1)
    @Max(5)
    private Integer driverRating;

    @Column(length = 500)
    private String comment;

    @Column(
            name = "created_at",
            updatable = false,
            nullable = false
    )
    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "ride_id",
            nullable = false,
            unique = true
    )
    private Ride ride;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "reviewer_id",
            nullable = false
    )
    private Passenger reviewer;

}