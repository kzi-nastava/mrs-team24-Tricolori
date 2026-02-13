package com.tricolori.backend.dto.block;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BlockRequest {
    @NotBlank(message = "Block reason is mandatory")
    private String blockReason;

    @Email
    private String userEmail;
}
