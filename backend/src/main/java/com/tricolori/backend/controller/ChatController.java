package com.tricolori.backend.controller;

import com.tricolori.backend.dto.chat.ChatMessageRequest;
import com.tricolori.backend.dto.chat.ChatMessageResponse;
import com.tricolori.backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat.send")
    public void sendMessage(ChatMessageRequest message) {
        log.info("Received message from user {} to user {}: {}",
                message.getSenderId(), message.getReceiverId(), message.getContent());

        ChatMessageResponse response = chatService.processMessage(message);

        log.info("Saved message with ID: {}", response.getId());

        messagingTemplate.convertAndSend(
                "/topic/chat/" + message.getReceiverId(),
                response
        );

        messagingTemplate.convertAndSend(
                "/topic/chat/" + message.getSenderId(),
                response
        );

        log.info("Message sent to topics for users {} and {}",
                message.getReceiverId(), message.getSenderId());
    }
}