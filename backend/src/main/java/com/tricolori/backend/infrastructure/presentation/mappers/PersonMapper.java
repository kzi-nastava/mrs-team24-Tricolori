package com.tricolori.backend.infrastructure.presentation.mappers;

import com.tricolori.backend.core.domain.models.Person;
import com.tricolori.backend.infrastructure.presentation.dtos.PersonDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PersonMapper {

    PersonDto toDto(Person person);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Person toEntity(PersonDto personDto);

}
