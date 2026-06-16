package com.example.leavemanagement.service;

import com.example.leavemanagement.entity.Notification;
import com.example.leavemanagement.entity.User;
import com.example.leavemanagement.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    // Create and save a notification
    public void sendNotification(User user, String message) {
        Notification notification = Notification.builder()
                .user(user)
                .message(message)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
    }

    // Get all notifications for a user
    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    // Get unread notifications
    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndIsRead(user, false);
    }

    // Count unread (for navbar badge)
    public long countUnread(User user) {
        return notificationRepository.countByUserAndIsRead(user, false);
    }

    // Mark all as read
    public void markAllAsRead(User user) {
        notificationRepository.markAllAsRead(user);
    }
}