package com.tricolori.backend.infrastructure.presentation.controllers;

import com.tricolori.backend.infrastructure.presentation.dtos.ChatConversationResponse;
import com.tricolori.backend.infrastructure.presentation.dtos.ChatMessageRequest;
import com.tricolori.backend.infrastructure.presentation.dtos.ChatMessageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/support")
@RequiredArgsConstructor
public class SupportController {

    /**
     * 2.11 - Get chat history between current user and admins
     * Returns all messages sent/received by current user with any admin
     */
    @GetMapping("/messages")
    @PreAuthorize("hasAnyRole('PASSENGER', 'DRIVER')")
    public ResponseEntity<List<ChatMessageResponse>> getMyMessages() {

        return ResponseEntity.ok(List.of());
    }

    /**
     * 2.11 - Send message to admin support (from passenger/driver)
     * Creates a message with sender = current user, recipient = any available admin
     */
    @PostMapping("/messages")
    @PreAuthorize("hasAnyRole('PASSENGER', 'DRIVER')")
    public ResponseEntity<ChatMessageResponse> sendMessageToSupport(@Valid @RequestBody ChatMessageRequest request) {

        return ResponseEntity.ok().build();
    }

    /**
     * 2.11 - Get all users who have messaged support (for admin)
     * Groups messages by user and shows last message info
     */
    @GetMapping("/conversations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ChatConversationResponse>> getAllConversations() {

        return ResponseEntity.ok(List.of());
    }

    /**
     * 2.11 - Get all messages with specific user (for admin)
     * Shows full conversation history between any admin and the specified user
     */
    @GetMapping("/messages/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ChatMessageResponse>> getMessagesWithUser(@PathVariable Long userId) {

        return ResponseEntity.ok(List.of());
    }

    /**
     * 2.11 - Send message to user as admin
     * Creates a message with sender = current admin, recipient = specified user
     */
    @PostMapping("/messages/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ChatMessageResponse> sendMessageToUser(
            @PathVariable Long userId,
            @Valid @RequestBody ChatMessageRequest request
    ) {

        return ResponseEntity.ok().build();
    }
}