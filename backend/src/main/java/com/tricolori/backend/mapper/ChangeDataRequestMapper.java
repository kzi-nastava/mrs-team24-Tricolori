package com.tricolori.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.tricolori.backend.dto.profile.ChangeDriverProfileDTO;
import com.tricolori.backend.dto.profile.ProfileRequest;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ChangeDataRequestMapper {
    @Mapping(source = "phoneNumber", target = "phoneNum")
    @Mapping(source = "pfp", target = "pfpUrl")
    ChangeDriverProfileDTO fromProfileRequest(ProfileRequest request);
}
