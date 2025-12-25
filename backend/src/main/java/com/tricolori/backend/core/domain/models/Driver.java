package com.tricolori.backend.core.domain.models;

import com.tricolori.backend.shared.enums.PersonRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity(name = "Driver")
@Table(name = "drivers")
@PrimaryKeyJoinColumn(name = "person_id")
@Getter @Setter @NoArgsConstructor
public class Driver extends Person {

    @OneToMany(
            mappedBy = "driver",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("date DESC")
    private List<DriverDailyLog> dailyLogs = new ArrayList<>();

    @OneToOne(
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL
    )
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    {
        this.setRole(PersonRole.DRIVER);
    }

}
