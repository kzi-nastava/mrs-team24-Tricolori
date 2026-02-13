package com.tricolori.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "tracking_tokens")
@Data
@NoArgsConstructor
public class TrackingToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private String email;

    @Column
    private String firstName;

    @Column
    private String lastName;

    @ManyToOne
    @JoinColumn(name = "person_id")
    private Person person; // only set if user is registered

    @ManyToOne
    @JoinColumn(name = "ride_id", nullable = false)
    private Ride ride;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private boolean used = false;

    public TrackingToken(String token, String email, String firstName, Ride ride) {
        this.token = token;
        this.email = email;
        this.firstName = firstName;
        this.ride = ride;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusDays(2); // valid for 2 days
    }

    public TrackingToken(String token, String email, String firstName, String lastName, Ride ride) {
        this(token, email, firstName, ride);
        this.lastName = lastName;
    }

    public boolean isRegisteredUser() {
        return person != null;
    }
}