package com.tricolori.backend.infrastructure.presentation.dtos;

import com.tricolori.backend.shared.enums.AccountStatus;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ChangeDriverProfileDTO {

    private String firstName;
    private String lastName;
    private String phoneNum;
    private String homeAddress;
    private String pfpUrl;

}