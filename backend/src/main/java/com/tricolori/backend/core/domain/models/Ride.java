package com.tricolori.backend.core.domain.models;

import com.tricolori.backend.shared.enums.RideStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "Ride")
@Table(name = "rides")
@Getter @Setter @NoArgsConstructor
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "created_at",
            updatable = false,
            nullable = false
    )
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "scheduled_for")
    private LocalDateTime scheduledFor;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private RideStatus status;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Min(1)
    private Double price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "vehicle_specification_id",
            nullable = false
    )
    private VehicleSpecification vehicleSpecification;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "ride_passengers",
            joinColumns = @JoinColumn(name = "ride_id"),
            inverseJoinColumns = @JoinColumn(name = "passenger_id")
    )
    private List<Passenger> passengers = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(
            name = "route_id",
            nullable = false
    )
    private Route route;

    public Passenger getMainPassenger() {
        if (passengers == null || passengers.isEmpty()) {
            return null;
        }
        return passengers.getFirst();
    }

}
