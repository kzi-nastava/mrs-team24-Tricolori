package com.tricolori.backend.repository;

import com.tricolori.backend.entity.Message;
import com.tricolori.backend.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE " +
            "(m.sender.id = :userId1 AND m.recipient.id = :userId2) OR " +
            "(m.sender.id = :userId2 AND m.recipient.id = :userId1) " +
            "ORDER BY m.createdAt ASC")
    List<Message> findChatMessages(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    @Query("SELECT DISTINCT m.sender FROM Message m WHERE m.recipient.id = :adminId AND m.sender.id != :adminId " +
            "UNION " +
            "SELECT DISTINCT m.recipient FROM Message m WHERE m.sender.id = :adminId AND m.recipient.id != :adminId")
    List<Person> findUsersWithChats(@Param("adminId") Long adminId);

    @Query("SELECT m FROM Message m WHERE " +
            "(m.sender.id = :userId1 AND m.recipient.id = :userId2) OR " +
            "(m.sender.id = :userId2 AND m.recipient.id = :userId1) " +
            "ORDER BY m.createdAt DESC LIMIT 1")
    Message findLastMessageBetweenUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    @Query("SELECT COUNT(m) > 0 FROM Message m WHERE " +
            "m.recipient.id = :recipientId AND m.sender.id = :senderId AND m.isRead = false")
    boolean hasUnreadMessages(@Param("recipientId") Long recipientId, @Param("senderId") Long senderId);

    @Modifying
    @Query("UPDATE Message m SET m.isRead = true, m.readAt = CURRENT_TIMESTAMP WHERE " +
            "m.recipient.id = :recipientId AND m.sender.id = :senderId AND m.isRead = false")
    void markMessagesAsRead(@Param("recipientId") Long recipientId, @Param("senderId") Long senderId);
}