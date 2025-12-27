package com.tricolori.backend.infrastructure.presentation.dtos;

import java.time.LocalDateTime;

public record ChatConversationResponse(
        Long userId,
        String userName,
        String userRole,
        String lastMessage,
        LocalDateTime lastMessageTime
) {}