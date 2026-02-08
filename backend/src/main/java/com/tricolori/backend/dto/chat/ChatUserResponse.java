package com.tricolori.backend.dto.chat;

import com.tricolori.backend.enums.PersonRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatUserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private PersonRole role;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private boolean hasUnread;
}