package com.example.gestiondetareas.repository;

import com.example.gestiondetareas.model.Message;
import com.example.gestiondetareas.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByReceiverAndReadFalse(User receiver);
    List<Message> findByReceiverOrderBySentAtDesc(User receiver);
    List<Message> findBySenderOrderBySentAtDesc(User sender);
    
    @Query("SELECT m FROM Message m WHERE (m.sender = :user OR m.receiver = :user) ORDER BY m.sentAt DESC")
    List<Message> findUserConversations(@Param("user") User user);
    
    Long countByReceiverAndReadFalse(User receiver);
}