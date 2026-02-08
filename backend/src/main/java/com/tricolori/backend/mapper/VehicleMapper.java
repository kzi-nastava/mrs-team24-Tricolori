package com.tricolori.backend.mapper;

import com.tricolori.backend.dto.vehicle.VehicleLocationResponse;
import com.tricolori.backend.entity.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.tricolori.backend.dto.auth.AdminDriverRegistrationRequest;
import com.tricolori.backend.dto.auth.RegisterVehicleSpecification;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VehicleMapper {

    @Mapping(source = "vehicleModel", target = "model")
    @Mapping(source = "vehicleType", target = "type")
    RegisterVehicleSpecification fromAdminDriverRegistration(AdminDriverRegistrationRequest request);


    @Mapping(target = "vehicleId", source = "id")
    @Mapping(target = "latitude", source = "location.latitude")
    @Mapping(target = "longitude", source = "location.longitude")
    VehicleLocationResponse toLocationDto(Vehicle vehicle);
}
