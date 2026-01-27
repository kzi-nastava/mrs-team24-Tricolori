package com.tricolori.backend.dto.ride;

import java.time.LocalDateTime;

public record OrderRequest(
    RouteDto route,
    boolean babyFriendly,
    boolean petFriendly,
    LocalDateTime orderedAt,
    LocalDateTime orderedFor    // null if it's not a reservation
) 
{}
