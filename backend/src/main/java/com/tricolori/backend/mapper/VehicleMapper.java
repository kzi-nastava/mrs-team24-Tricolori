package com.tricolori.backend.mapper;

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
}
