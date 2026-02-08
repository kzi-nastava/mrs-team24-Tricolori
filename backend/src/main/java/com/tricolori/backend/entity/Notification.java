package com.tricolori.backend.entity;

import com.tricolori.backend.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity(name = "Notification")
@Table(name = "notifications")
@Getter @Setter @NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(
            nullable = false,
            updatable = false
    )
    @CreationTimestamp
    private LocalDateTime time;

    @Column(nullable = false)
    private boolean opened = false;

    @Column(nullable = false, length = 1000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    @Column(name = "ride_id")
    private Long rideId;

    // Optional: action URL for frontend navigation
    @Column(name = "action_url", length = 500)
    private String actionUrl;

    // Additional metadata for specific notification types
    @Column(name = "driver_name", length = 255)
    private String driverName;

    @Column(name = "passenger_name", length = 255)
    private String passengerName;

    public Notification(String email, String content, NotificationType type, Long rideId) {
        this.email = email;
        this.content = content;
        this.type = type;
        this.rideId = rideId;
    }
}