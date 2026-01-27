package com.tricolori.backend.core.services;

import org.springframework.stereotype.Service;

import com.tricolori.backend.core.domain.models.Location;
import com.tricolori.backend.core.domain.models.Person;
import com.tricolori.backend.core.domain.models.Vehicle;
import com.tricolori.backend.core.domain.models.VehicleSpecification;
import com.tricolori.backend.core.domain.repositories.VehicleRepository;
import com.tricolori.backend.core.domain.repositories.VehicleSpecificationRepository;
import com.tricolori.backend.infrastructure.presentation.dtos.Auth.RegisterVehicleSpecification;
import com.tricolori.backend.infrastructure.presentation.dtos.Profile.ProfileResponse;
import com.tricolori.backend.infrastructure.presentation.dtos.Vehicle.VehicleDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VehicleService {
    private final VehicleRepository vehicleRepository;
    private final VehicleSpecificationRepository specificationRepository;

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

    public VehicleSpecification registerVehicleSpecification(RegisterVehicleSpecification request) {
        VehicleSpecification specification = new VehicleSpecification();

        specification.setBabyFriendly(request.babyFriendly());
        specification.setPetFriendly(request.petFriendly());
        specification.setModel(request.model());
        specification.setType(request.type());
        specification.setNumSeats(request.seatNumber());

        return specificationRepository.save(specification);
    }

    public Vehicle registerVehicle(VehicleSpecification specs, String registrationPlate) {
        Vehicle vehicle = new Vehicle();

        vehicle.setSpecification(specs);
        vehicle.setPlateNum(registrationPlate);
        vehicle.setModel(specs.getModel());
        vehicle.setLocation(new Location(1.0, 1.0));

        return vehicleRepository.save(vehicle);
    }
}
