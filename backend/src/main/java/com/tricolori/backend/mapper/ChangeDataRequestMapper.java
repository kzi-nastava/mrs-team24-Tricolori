package com.tricolori.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.tricolori.backend.dto.profile.ChangeDataRequestResponse;
import com.tricolori.backend.dto.profile.ChangeDriverProfileDTO;
import com.tricolori.backend.dto.profile.ProfileRequest;
import com.tricolori.backend.entity.ChangeDataRequest;
import com.tricolori.backend.entity.Driver;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ChangeDataRequestMapper {
    @Mapping(source = "phoneNumber", target = "phoneNum")
    @Mapping(source = "pfp", target = "pfpUrl")
    ChangeDriverProfileDTO fromProfileRequest(ProfileRequest request);

    @Mapping(source = "phoneNum", target = "phoneNumber")
    @Mapping(source = "pfpUrl", target = "pfp")
    ProfileRequest toProfileRequest(ChangeDriverProfileDTO dto);

    @Mapping(source = "profile.id", target = "driverId")
    @Mapping(source = "changes", target = "newValues")
    @Mapping(source = "profile", target = "oldValues")
    @Mapping(source = "profile.email", target = "email")
    ChangeDataRequestResponse backToAdmin(ChangeDataRequest request);

    @Mapping(source = "phoneNum", target = "phoneNum")
    @Mapping(source = "pfpUrl", target = "pfpUrl")
    ChangeDriverProfileDTO driverToProfileDTO(Driver driver);
}
