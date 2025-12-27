package com.tricolori.backend.infrastructure.presentation.dtos;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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