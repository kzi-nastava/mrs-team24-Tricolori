package com.example.mobile.dto.auth;

import com.example.mobile.dto.profile.PersonDto;

public class LoginResponse {
    public String accessToken;
    public PersonDto personDto;

    public LoginResponse(String accessToken, PersonDto personDto) {
        this.accessToken = accessToken;
        this.personDto = personDto;
    }
}
