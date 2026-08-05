package com.cvns.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.cvns.entities.Notification;

public interface NotificationRepository extends JpaRepository<Notification,Long> {
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long id);
    long countByRecipientIdAndReadFalse(Long id);
    boolean existsByRecipientIdAndTitleAndMessage(Long id,String title,String message);
    void deleteByRecipientId(Long recipientId);
}
