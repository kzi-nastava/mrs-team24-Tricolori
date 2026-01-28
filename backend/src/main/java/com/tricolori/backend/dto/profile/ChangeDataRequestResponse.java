package com.tricolori.backend.dto.profile;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeDataRequestResponse {
    long id;
    long driverId;
    String email;
    ChangeDriverProfileDTO oldValues;
    ChangeDriverProfileDTO newValues;
    LocalDateTime createdAt;
}
