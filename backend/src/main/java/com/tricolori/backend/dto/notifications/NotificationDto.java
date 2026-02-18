package com.tricolori.backend.dto.notifications;

import com.tricolori.backend.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class NotificationDto {
    private Long id;
    private String email;
    private LocalDateTime time;
    private boolean opened;
    private String content;
    private NotificationType type;
    private Long rideId;
    private String actionUrl;
    private String driverName;
    private String passengerName;
}