package com.tricolori.backend.infrastructure.presentation.dtos.Profile;

import com.tricolori.backend.core.domain.models.Person;
import com.tricolori.backend.infrastructure.presentation.dtos.VehicleDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse
{
    private String email;
    private String firstName;
    private String lastName;
    private String homeAddress;
    private String phoneNumber;
    private String pfp;
    private VehicleDto vehicle; // null for non-drivers
    private Double activeHours;  // null for non-drivers

    public static ProfileResponse fromPerson(Person person) {
        return new ProfileResponse(
            person.getEmail(),
            person.getFirstName(),
            person.getLastName(),
            person.getHomeAddress(),
            person.getPhoneNum(),
            person.getPfpUrl(),
            null, // Explicitly null, for now
            null  // Explicitly null, for now
        );
    }
}
