package com.tricolori.backend.dto.report;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequest {
    @NotNull(message = "From date is mandatory")
    private LocalDateTime from;

    @NotNull(message = "To date is mandatory")
    private LocalDateTime to;
}
