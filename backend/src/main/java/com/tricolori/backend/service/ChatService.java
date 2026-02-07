package com.tricolori.backend.service;

import com.tricolori.backend.dto.chat.ChatMessageRequest;
import com.tricolori.backend.dto.chat.ChatMessageResponse;
import com.tricolori.backend.dto.chat.ChatUserResponse;
import com.tricolori.backend.entity.Message;
import com.tricolori.backend.entity.Person;
import com.tricolori.backend.enums.PersonRole;
import com.tricolori.backend.repository.MessageRepository;
import com.tricolori.backend.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
        message.setRead(false);

        Message savedMessage = messageRepository.save(message);

        return new ChatMessageResponse(
                savedMessage.getId(),
                savedMessage.getSender().getId(),
                savedMessage.getRecipient().getId(),
                savedMessage.getContent(),
                savedMessage.getCreatedAt()
        );
    }

    @Transactional
    public List<ChatMessageResponse> getChatHistory(Long userId1, Long userId2) {
        // Mark messages as read when fetching chat history
        messageRepository.markMessagesAsRead(userId1, userId2);

        return messageRepository.findChatMessages(userId1, userId2)
                .stream()
                .map(message -> new ChatMessageResponse(
                        message.getId(),
                        message.getSender().getId(),
                        message.getRecipient().getId(),
                        message.getContent(),
                        message.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    public boolean isAdminAvailable() {
        return personRepository.existsByRole(PersonRole.ROLE_ADMIN);
    }

    public List<ChatUserResponse> getActiveChatsForAdmin(Long adminId) {
        List<Person> users = messageRepository.findUsersWithChats(adminId);

        return users.stream()
                .map(user -> {
                    Message lastMessage = messageRepository.findLastMessageBetweenUsers(adminId, user.getId());
                    boolean hasUnread = messageRepository.hasUnreadMessages(adminId, user.getId());

                    return new ChatUserResponse(
                            user.getId(),
                            user.getFirstName(),
                            user.getLastName(),
                            user.getEmail(),
                            user.getRole(),
                            lastMessage != null ? lastMessage.getContent() : "No messages",
                            lastMessage != null ? lastMessage.getCreatedAt() : null,
                            hasUnread
                    );
                })
                .collect(Collectors.toList());
    }

    public Long getAdminId() {
        Person admin = personRepository.findByRole(PersonRole.ROLE_ADMIN)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No admin found"));
        return admin.getId();
    }
}