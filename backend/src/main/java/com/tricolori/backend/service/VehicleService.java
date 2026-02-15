package com.tricolori.backend.service;

import com.tricolori.backend.dto.vehicle.VehicleLocationResponse;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import com.tricolori.backend.entity.Location;
import com.tricolori.backend.entity.Person;
import com.tricolori.backend.entity.Vehicle;
import com.tricolori.backend.entity.VehicleSpecification;
import com.tricolori.backend.repository.VehicleRepository;
import com.tricolori.backend.repository.VehicleSpecificationRepository;
import com.tricolori.backend.dto.auth.RegisterVehicleSpecification;
import com.tricolori.backend.dto.profile.ProfileResponse;
import com.tricolori.backend.dto.vehicle.VehicleDto;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

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

    public List<VehicleLocationResponse> getAllActiveVehicles() {
        return vehicleRepository.findAllWithLocation()
                .stream()
                .map(vehicle -> new VehicleLocationResponse(
                        vehicle.getId(),
                        vehicle.getModel(),
                        vehicle.getPlateNum(),
                        vehicle.getLocation().getLatitude(),
                        vehicle.getLocation().getLongitude(),
                        vehicle.isAvailable()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public VehicleLocationResponse updateVehicleLocation(Long vehicleId, double latitude, double longitude) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        // Update location
        Location location = vehicle.getLocation();
        if (location == null) {
            location = new Location(latitude, longitude);
            vehicle.setLocation(location);
        } else {
            location.setLatitude(latitude);
            location.setLongitude(longitude);
        }

        vehicleRepository.save(vehicle);

        return new VehicleLocationResponse(
                vehicle.getId(), vehicle.getModel(), vehicle.getPlateNum(),
                latitude, longitude, vehicle.isAvailable()
        );
    }
}
