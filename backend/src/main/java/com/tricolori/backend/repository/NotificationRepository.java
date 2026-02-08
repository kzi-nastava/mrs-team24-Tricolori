package com.tricolori.backend.repository;

import com.tricolori.backend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByEmailOrderByTimeDesc(String email);

    List<Notification> findByEmailAndOpenedOrderByTimeDesc(String email, boolean opened);

    long countByEmailAndOpened(String email, boolean opened);

    void deleteByEmail(String email);
}