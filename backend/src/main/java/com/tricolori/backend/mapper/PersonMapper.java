package com.tricolori.backend.mapper;

import com.tricolori.backend.entity.Person;
import com.tricolori.backend.entity.Driver;
import com.tricolori.backend.entity.Passenger;
import com.tricolori.backend.dto.profile.PersonDto;
import com.tricolori.backend.dto.profile.ProfileResponse;
import com.tricolori.backend.dto.block.ActivePersonStatus;
import com.tricolori.backend.dto.profile.DriverDto;
import com.tricolori.backend.dto.profile.PassengerDto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PersonMapper {
    @Mapping(source = "createdAt", target = "registrationDate")
    @Mapping(source = "accountStatus", target = "status")
    ActivePersonStatus toActivePersonStatus(Person person);

    default Page<ActivePersonStatus> toActivePersonStatusPage(Page<Person> personPage) {
        return personPage.map(this::toActivePersonStatus);
    }

    PersonDto toDto(Person person);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Person toEntity(PersonDto personDto);

    @Mapping(source = "phoneNum", target = "phoneNumber")
    @Mapping(source = "pfpUrl", target = "pfp")
    @Mapping(target = "vehicle", ignore = true)
    @Mapping(target = "activeHours", constant = "0.0")
    ProfileResponse toProfileResponse(Person person);

    // DRIVER
    @Mapping(target = "rating", source = "rating")
    DriverDto toDriverDto(Driver driver, Double rating);

    // PASSENGER
    @Mapping(target = "mainPassenger", ignore = true)
    PassengerDto toPassengerDto(Passenger passenger);
}
