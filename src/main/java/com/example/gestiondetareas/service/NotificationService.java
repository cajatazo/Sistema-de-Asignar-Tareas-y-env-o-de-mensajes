package com.example.gestiondetareas.service;

import com.example.gestiondetareas.model.Notification;
import com.example.gestiondetareas.model.NotificationType;
import com.example.gestiondetareas.model.User;
import com.example.gestiondetareas.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private UserService userService;
    
    public List<Notification> getUserNotifications(Long userId) {
        User user = userService.findUserById(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    public List<Notification> getUnreadNotifications(Long userId) {
        User user = userService.findUserById(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return notificationRepository.findByUserAndReadFalseOrderByCreatedAtDesc(user);
    }
    
    public Notification createNotification(String title, String message, NotificationType type, User user) {
        Notification notification = new Notification(title, message, type, user);
        return notificationRepository.save(notification);
    }
    
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }
    
    public void markAllAsRead(Long userId) {
        User user = userService.findUserById(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        List<Notification> unreadNotifications = notificationRepository.findByUserAndReadFalseOrderByCreatedAtDesc(user);
        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
    }
    
    public Long getUnreadNotificationCount(Long userId) {
        User user = userService.findUserById(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return notificationRepository.countByUserAndReadFalse(user);
    }
}