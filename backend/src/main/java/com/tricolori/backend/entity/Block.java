package com.tricolori.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Embeddable
@Data @NoArgsConstructor @AllArgsConstructor
public class Block {

    @Column(name = "blocked_at")
    private LocalDateTime blockedAt;

    @Column(name = "block_reason")
    private String blockReason;

}
