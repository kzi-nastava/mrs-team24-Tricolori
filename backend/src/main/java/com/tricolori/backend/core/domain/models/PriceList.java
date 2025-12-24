package com.tricolori.backend.core.domain.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;

@Entity(name = "PriceList")
@Table(name = "price_lists")
@Getter @Setter @NoArgsConstructor
public class PriceList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "created_at",
            updatable = false,
            nullable = false
    )
    @CreationTimestamp
    private LocalDate createdAt;

    @Column(name = "standard_price")
    private double standardPrice;

    @Column(name = "luxury_price")
    private double luxuryPrice;

    @Column(name = "van_price")
    private double vanPrice;

    @Column(name = "km_price")
    private double kmPrice;

}
