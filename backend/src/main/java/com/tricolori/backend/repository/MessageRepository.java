package com.tricolori.backend.repository;

import com.tricolori.backend.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findBySenderIdAndRecipientIdOrderByCreatedAtAsc(Long senderId, Long recipientId);

    List<Message> findByRecipientIdOrSenderIdOrderByCreatedAtAsc(Long recipientId, Long senderId);
}