package com.tricolori.backend.mapper;

import com.tricolori.backend.dto.driver.DriverDailyLogResponse;
import com.tricolori.backend.entity.DriverDailyLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DriverDailyLogMapper {

    @Mapping(target = "driverId", source = "driver.id")
    DriverDailyLogResponse toResponse(DriverDailyLog entity);
}