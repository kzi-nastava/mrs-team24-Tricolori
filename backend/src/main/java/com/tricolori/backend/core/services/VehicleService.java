package com.tricolori.backend.core.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tricolori.backend.core.domain.models.Person;
import com.tricolori.backend.core.domain.models.VehicleSpecification;
import com.tricolori.backend.core.domain.repositories.VehicleRepository;
import com.tricolori.backend.infrastructure.presentation.dtos.Profile.ProfileResponse;
import com.tricolori.backend.infrastructure.presentation.dtos.Vehicle.VehicleDto;

@Service
public class VehicleService {
    @Autowired
    private VehicleRepository vehicleRepository;

    public void fillDriverVehicleData(Person currentUser, ProfileResponse response) {
        vehicleRepository.findByDriverId(currentUser.getId()).ifPresent(vehicle -> {
            VehicleSpecification specs = vehicle.getSpecification();
            
            VehicleDto vehicleDto = new VehicleDto(
                vehicle.getModel(),
                specs.getType().toString(),
                vehicle.getPlateNum(),
                specs.getNumSeats(),
                specs.isBabyFriendly(),
                specs.isPetFriendly()
            );

            response.setVehicle(vehicleDto);
        });
    }
}
