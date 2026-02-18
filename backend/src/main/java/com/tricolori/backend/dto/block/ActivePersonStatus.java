package com.tricolori.backend.dto.block;

import com.tricolori.backend.enums.AccountStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ActivePersonStatus {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String registrationDate;
    private AccountStatus status;
}
