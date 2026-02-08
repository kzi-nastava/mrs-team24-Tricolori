package com.tricolori.backend.enums;

public enum NotificationType {
    // Passenger notifications
    RIDE_STARTING,
    RIDE_CANCELLED,
    RIDE_REJECTED,
    ADDED_TO_RIDE,
    RIDE_COMPLETED,
    RATING_REMINDER,
    RIDE_REMINDER,

    // Driver notifications
    NEW_RIDE_REQUEST,
    UPCOMING_RIDE_REMINDER,
    RATING_RECEIVED,
    RIDE_STARTED,

    // Admin notifications
    RIDE_REPORT,
    NEW_REGISTRATION,
    PROFILE_CHANGE_REQUEST,

    NEW_CHAT_MESSAGE,

    // General
    GENERAL
}