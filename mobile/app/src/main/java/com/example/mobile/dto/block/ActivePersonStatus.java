package com.example.mobile.dto.block;

import com.example.mobile.enums.AccountStatus;

public class ActivePersonStatus {
    private long id;
    private String firstName;
    private String lastName;
    private String email;
    private String registrationDate;
    private AccountStatus status;

    public ActivePersonStatus() {}

    public ActivePersonStatus(long id, String firstName, String lastName,
                              String email, String registrationDate, AccountStatus status) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.registrationDate = registrationDate;
        this.status = status;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(String registrationDate) { this.registrationDate = registrationDate; }

    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }
}
