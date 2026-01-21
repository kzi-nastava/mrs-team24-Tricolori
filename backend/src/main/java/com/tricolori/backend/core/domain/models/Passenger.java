package com.tricolori.backend.core.domain.models;

import com.tricolori.backend.shared.enums.PersonRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "Passenger")
@Table(name = "passengers")
@PrimaryKeyJoinColumn(name = "person_id")
@Getter @Setter @NoArgsConstructor
public class Passenger extends Person {

    {
        this.setRole(PersonRole.ROLE_PASSENGER);
    }

}
