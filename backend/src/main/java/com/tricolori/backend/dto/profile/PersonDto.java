package com.tricolori.backend.dto.profile;

import com.tricolori.backend.entity.Block;
import com.tricolori.backend.enums.AccountStatus;
import com.tricolori.backend.enums.PersonRole;

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

