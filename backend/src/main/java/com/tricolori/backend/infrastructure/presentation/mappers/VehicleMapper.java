package com.tricolori.backend.infrastructure.presentation.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.tricolori.backend.infrastructure.presentation.dtos.Auth.AdminDriverRegistrationRequest;
import com.tricolori.backend.infrastructure.presentation.dtos.Auth.RegisterVehicleSpecification;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VehicleMapper {

    @Mapping(source = "vehicleModel", target = "model")
    @Mapping(source = "vehicleType", target = "type")
    RegisterVehicleSpecification fromAdminDriverRegistration(AdminDriverRegistrationRequest request);
}
