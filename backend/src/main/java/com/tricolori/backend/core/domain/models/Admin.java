package com.tricolori.backend.core.domain.models;

import com.tricolori.backend.shared.enums.PersonRole;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "Admin")
@Table(name = "admins")
@PrimaryKeyJoinColumn(name = "person_id")
@Getter @Setter @NoArgsConstructor
public class Admin extends Person {

    {
        this.setRole(PersonRole.ROLE_ADMIN);
    }

}
