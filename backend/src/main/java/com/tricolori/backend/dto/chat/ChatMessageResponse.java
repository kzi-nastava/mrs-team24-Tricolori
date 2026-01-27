package com.tricolori.backend.dto.chat;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        Long id,
        String content,
        LocalDateTime createdAt,
        Long senderId,
        String senderName,
        Long recipientId,
        String recipientName
) {}