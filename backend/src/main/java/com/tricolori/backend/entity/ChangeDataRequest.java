package com.tricolori.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import com.tricolori.backend.dto.profile.ChangeDriverProfileDTO;
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
            name = "profile_id",
            nullable = false
    )
    private Driver profile;

    @Embedded
    private ChangeDriverProfileDTO changes;


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

    public boolean isPending() {
        return reviewedAt == null;
    }

}
