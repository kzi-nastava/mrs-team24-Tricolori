package com.tricolori.backend.dto.ride;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RideHistoryResponse {
    private Long id;
    private String pickupAddress;
    private String destinationAddress;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Double price;
    private Double distance;
    private String status;
}