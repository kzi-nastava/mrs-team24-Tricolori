package com.tricolori.backend.dto.chat;

import java.time.LocalDateTime;

public record ChatConversationResponse(
        Long userId,
        String userName,
        String userRole,
        String lastMessage,
        LocalDateTime lastMessageTime
) {}