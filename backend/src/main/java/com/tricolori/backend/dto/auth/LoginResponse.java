package com.tricolori.backend.dto.auth;

import com.tricolori.backend.dto.profile.PersonDto;

public record LoginResponse(
        String accessToken,
        PersonDto personDto
) {
}
