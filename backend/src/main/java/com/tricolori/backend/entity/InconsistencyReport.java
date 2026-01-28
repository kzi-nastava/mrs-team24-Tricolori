package com.tricolori.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity(name = "InconsistencyReport")
@Table(name = "inconsistency_reports")
@Getter @Setter @NoArgsConstructor
public class InconsistencyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            nullable = false,
            length = 500
    )
    private String description;

    @Column(
            name = "reported_at",
            updatable = false,
            nullable = false
    )
    @CreationTimestamp
    private LocalDateTime reportedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "ride_id",
            nullable = false
    )
    private Ride ride;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "reporter_id",
            nullable = false
    )
    private Passenger reporter;

}