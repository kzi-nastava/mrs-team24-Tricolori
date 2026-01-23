package com.tricolori.backend.infrastructure.presentation.dtos.Profile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileRequest {
    private String firstName;
    private String lastName;
    private String homeAddress;
    private String phoneNumber;
    private String pfp;
}
