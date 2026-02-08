package com.example.mobile.dto.profile;

import com.example.mobile.enums.AccountStatus;
import com.example.mobile.enums.PersonRole;
import com.example.mobile.model.Block;

public class PersonDto {
    public Long id;
    public PersonRole role;
    public String firstName;
    public String lastName;
    public String phoneNum;
    public String homeAddress;
    public String email;
    public AccountStatus accountStatus;
    public String pfpUrl;
    public Block block;
}
