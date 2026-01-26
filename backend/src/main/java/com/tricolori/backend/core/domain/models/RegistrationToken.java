package com.tricolori.backend.core.domain.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "registration_tokens")
@Getter @Setter @NoArgsConstructor
public class RegistrationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime activatedAt;

    @Column(nullable = false)
    private boolean used = false;

    // Factory method
    public static RegistrationToken createForPerson(Person person) {
        RegistrationToken token = new RegistrationToken();
        token.setToken(UUID.randomUUID().toString());
        token.setPerson(person);
        token.setExpiresAt(LocalDateTime.now().plusHours(24));
        return token;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !used && !isExpired();
    }
}