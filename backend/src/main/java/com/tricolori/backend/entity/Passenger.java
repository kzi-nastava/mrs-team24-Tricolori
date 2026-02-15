package com.tricolori.backend.entity;

import com.tricolori.backend.enums.PersonRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity(name = "Passenger")
@Table(name = "passengers")
@PrimaryKeyJoinColumn(name = "person_id")
@Getter @Setter @NoArgsConstructor
@SuperBuilder
public class Passenger extends Person {

    {
        this.setRole(PersonRole.ROLE_PASSENGER);
    }

}
