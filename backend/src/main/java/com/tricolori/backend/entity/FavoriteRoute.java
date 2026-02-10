package com.tricolori.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity(name = "FavoriteRoute")
@Table(
        name = "favorite_routes",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_person_route", columnNames = { "person_id", "route_id" })
        }
)
@Getter @Setter @NoArgsConstructor
public class FavoriteRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "person_id",
            nullable = false
    )
    private Person person;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "route_id",
            nullable = false
    )
    private Route route;

    @Column(
            nullable = false,
            updatable = false
    )
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(
        nullable = false,
        length = 100
    )
    private String title;
}
