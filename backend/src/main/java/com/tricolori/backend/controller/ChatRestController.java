package com.tricolori.backend.controller;

import com.tricolori.backend.dto.chat.ChatMessageResponse;
import com.tricolori.backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;

    @GetMapping("/history")
    public ResponseEntity<List<ChatMessageResponse>> getChatHistory(
            @RequestParam Long userId1,
            @RequestParam Long userId2) {
        List<ChatMessageResponse> messages = chatService.getChatHistory(userId1, userId2);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/admin-available")
    public ResponseEntity<Map<String, Boolean>> checkAdminAvailable() {
        boolean available = chatService.isAdminAvailable();
        return ResponseEntity.ok(Map.of("available", available));
    }
}