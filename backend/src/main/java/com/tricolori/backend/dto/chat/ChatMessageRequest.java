package com.tricolori.backend.dto.chat;

import lombok.Data;

@Data
public class ChatMessageRequest {
        private Long senderId;
        private Long receiverId;
        private String content;
}