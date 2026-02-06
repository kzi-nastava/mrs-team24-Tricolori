package com.tricolori.backend.service;

import com.tricolori.backend.dto.chat.ChatMessageRequest;
import com.tricolori.backend.dto.chat.ChatMessageResponse;
import com.tricolori.backend.entity.Message;
import com.tricolori.backend.entity.Person;
import com.tricolori.backend.repository.MessageRepository;
import com.tricolori.backend.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final MessageRepository messageRepository;
    private final PersonRepository personRepository;

    public ChatMessageResponse processMessage(ChatMessageRequest request) {
        Person sender = personRepository.findById(request.getSenderId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        Person recipient = personRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        Message message = new Message();
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setContent(request.getContent());

        Message savedMessage = messageRepository.save(message);

        return new ChatMessageResponse(
                savedMessage.getId(),
                savedMessage.getSender().getId(),
                savedMessage.getRecipient().getId(),
                savedMessage.getContent(),
                savedMessage.getCreatedAt()
        );
    }
}