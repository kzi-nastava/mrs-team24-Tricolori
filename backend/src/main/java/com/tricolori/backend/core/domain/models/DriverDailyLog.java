package com.tricolori.backend.core.domain.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity(name = "DriverDailyLog")
@Table(
        name = "driver_daily_logs",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_driver_date", columnNames = { "driver_id", "date" })
        }
)
@Getter @Setter @NoArgsConstructor
public class DriverDailyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "driver_id",
            nullable = false
    )
    private Driver driver;

    @Column(
            nullable = false,
            updatable = false
    )
    @CreationTimestamp
    private LocalDate date;

    @Column(name = "active_time_seconds", nullable = false)
    private Long activeTimeSeconds = 0L;

    private boolean active;

    @Column(name = "last_activation_at")
    private LocalDateTime lastActivationAt;

}
