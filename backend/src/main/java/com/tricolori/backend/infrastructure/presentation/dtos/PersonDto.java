package com.tricolori.backend.infrastructure.presentation.dtos;

import com.tricolori.backend.core.domain.models.Block;
import com.tricolori.backend.shared.enums.AccountStatus;
import com.tricolori.backend.shared.enums.PersonRole;

public record PersonDto(
        Long id,
        PersonRole role,
        String firstName,
        String lastName,
        String phoneNum,
        String homeAddress,
        String email,
        AccountStatus accountStatus,
        String pfpUrl,
        Block block
) {
}

