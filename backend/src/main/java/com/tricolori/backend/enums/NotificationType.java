package com.tricolori.backend.enums;

public enum NotificationType {
    // Passenger notifications
    RIDE_STARTING,
    RIDE_CANCELLED,
    ADDED_TO_RIDE,
    RIDE_COMPLETED,
    DRIVER_ASSIGNED,
    RATING_REMINDER,

    // Driver notifications
    NEW_RIDE_REQUEST,
    UPCOMING_RIDE_REMINDER,
    PASSENGER_ADDED,
    RATING_RECEIVED,
    RIDE_ACCEPTED,
    RIDE_STARTED,

    // Admin notifications
    RIDE_REPORT,
    DRIVER_ISSUE,
    SYSTEM_ALERT,
    NEW_REGISTRATION,

    // General
    GENERAL
}