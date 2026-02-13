package com.tricolori.backend.dto.report;

import java.time.LocalDateTime;

import com.tricolori.backend.enums.ReportScope;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminReportRequest {
    @NotNull(message = "From date is mandatory")
    private LocalDateTime from;

    @NotNull(message = "To date is mandatory")
    private LocalDateTime to;

    @NotNull(message = "Report scope is mandatory")
    private ReportScope scope;

    @Email
    private String individualEmail;
}
