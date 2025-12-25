package com.tricolori.backend.core.domain.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity(name = "ChangeDataRequest")
@Table(name = "change_data_requests")
@Getter @Setter @NoArgsConstructor
public class ChangeDataRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "old_profile_id",
            nullable = false
    )
    private Driver oldProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "new_profile_id",
            nullable = false
    )
    private Driver newProfile;

    @Column(
            nullable = false,
            updatable = false
    )
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Boolean approved = false;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

}