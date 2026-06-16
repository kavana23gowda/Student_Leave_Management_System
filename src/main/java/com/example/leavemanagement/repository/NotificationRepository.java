package com.example.leavemanagement.repository;

import com.example.leavemanagement.entity.Notification;
import com.example.leavemanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Get all notifications for a user (latest first)
    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    // Get only unread notifications for a user
    List<Notification> findByUserAndIsRead(User user, boolean isRead);

    // Count unread notifications (shown as badge on navbar)
    long countByUserAndIsRead(User user, boolean isRead);

    // Mark all notifications as read for a user
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user = :user")
    void markAllAsRead(@Param("user") User user);
}