package com.tricolori.backend.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChatMessageRequest {

        @NotNull(message = "Sender ID is required")
        private Long senderId;

        @NotNull(message = "Receiver ID is required")
        private Long receiverId;

        @NotBlank(message = "Message content cannot be empty")
        @Size(max = 1000, message = "Message must not exceed 1000 characters")
        private String content;
}