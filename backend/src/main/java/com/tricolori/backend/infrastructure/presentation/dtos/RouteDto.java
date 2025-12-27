package com.tricolori.backend.infrastructure.presentation.dtos;

import java.util.List;

import com.tricolori.backend.core.domain.models.Address;

public record RouteDto(
    Address pickup,
    Address destination,
    List<Address> stops     // order in list determines the order in stops
) 
{}
