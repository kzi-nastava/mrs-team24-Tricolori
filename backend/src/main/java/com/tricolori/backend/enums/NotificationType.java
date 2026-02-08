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
    NEW_RIDE_REQUEST,
    UPCOMING_RIDE_REMINDER,
    RATING_RECEIVED,
    RIDE_STARTED,

    // Admin notifications
    RIDE_REPORT, // ok
    NEW_REGISTRATION,
    PROFILE_CHANGE_REQUEST,

    NEW_CHAT_MESSAGE,

    // General
    GENERAL
}