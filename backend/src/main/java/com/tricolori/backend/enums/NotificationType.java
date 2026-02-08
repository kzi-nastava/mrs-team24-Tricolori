package com.tricolori.backend.enums;

public enum NotificationType {
    // Passenger notifications
    RIDE_STARTING, // ok
    RIDE_CANCELLED, //ok
    RIDE_REJECTED, // ok
    ADDED_TO_RIDE, // ok
    RIDE_COMPLETED, // ok
    RATING_REMINDER,
    RIDE_REMINDER,

    // Driver notifications
    UPCOMING_RIDE_REMINDER,
    RATING_RECEIVED, // ok
    RIDE_STARTED, // ok

    // Admin notifications
    RIDE_REPORT, // ok
    NEW_REGISTRATION, //ok
    PROFILE_CHANGE_REQUEST, // ok

    NEW_CHAT_MESSAGE, //ok

    // General
    GENERAL
}