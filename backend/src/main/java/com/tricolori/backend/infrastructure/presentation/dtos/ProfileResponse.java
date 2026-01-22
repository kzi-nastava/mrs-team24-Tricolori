package com.tricolori.backend.infrastructure.presentation.dtos;

import com.tricolori.backend.core.domain.models.Person;

public record ProfileResponse(
    String email,
    String firstName,
    String lastName,
    String homeAddress,
    String phoneNumber,
    String pfp,
    VehicleDto vehicle, // null for non-drivers
    Double activeHours  // null for non-drivers
)
{
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
