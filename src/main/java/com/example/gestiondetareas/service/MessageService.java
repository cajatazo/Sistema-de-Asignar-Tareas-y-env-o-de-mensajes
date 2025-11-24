package com.example.gestiondetareas.service;

import com.example.gestiondetareas.model.Message;
import com.example.gestiondetareas.model.NotificationType;
import com.example.gestiondetareas.model.User;
import com.example.gestiondetareas.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MessageService {
    
    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private NotificationService notificationService;
    
    public List<Message> getReceivedMessages(Long userId) {
        User user = userService.findUserById(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return messageRepository.findByReceiverOrderBySentAtDesc(user);
    }
    
    public List<Message> getSentMessages(Long userId) {
        User user = userService.findUserById(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return messageRepository.findBySenderOrderBySentAtDesc(user);
    }
    
    public List<Message> getUserConversations(Long userId) {
        User user = userService.findUserById(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return messageRepository.findUserConversations(user);
    }
    
    public Message sendMessage(String subject, String content, Long senderId, Long receiverId) {
        User sender = userService.findUserById(senderId)
            .orElseThrow(() -> new RuntimeException("Remitente no encontrado"));
        User receiver = userService.findUserById(receiverId)
            .orElseThrow(() -> new RuntimeException("Destinatario no encontrado"));
        
        Message message = new Message(subject, content, sender, receiver);
        Message savedMessage = messageRepository.save(message);
        
        // Notificar al destinatario
        notificationService.createNotification(
            "Nuevo Mensaje",
            "Tienes un nuevo mensaje de " + sender.getFullName(),
            NotificationType.MESSAGE_RECEIVED,
            receiver
        );
        
        return savedMessage;
    }
    
    public void markAsRead(Long messageId) {
        messageRepository.findById(messageId).ifPresent(message -> {
            message.setRead(true);
            messageRepository.save(message);
        });
    }
    
    public Long getUnreadMessageCount(Long userId) {
        User user = userService.findUserById(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return messageRepository.countByReceiverAndReadFalse(user);
    }
    
    public Message getMessageById(Long messageId) {
        return messageRepository.findById(messageId)
            .orElseThrow(() -> new RuntimeException("Mensaje no encontrado"));
    }
}