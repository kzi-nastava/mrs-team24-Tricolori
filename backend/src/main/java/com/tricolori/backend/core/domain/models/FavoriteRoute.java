//package com.tricolori.backend.core.domain.models;
//
//import jakarta.persistence.*;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//@Entity(name = "FavoriteRoute")
//@Table(name = "favorite_routes")
//@Getter @Setter @NoArgsConstructor
//public class FavoriteRoute {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(name = "display_name")
//    private String displayName;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "passenger_id", nullable = false)
//    private Passenger passenger;
//
//    @OneToOne(fetch = FetchType.LAZY)
//    private Route route;
//
//}

/* TODO */