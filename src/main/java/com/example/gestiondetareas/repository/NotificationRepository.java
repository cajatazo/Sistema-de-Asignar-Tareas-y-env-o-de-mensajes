package com.example.gestiondetareas.repository;

import com.example.gestiondetareas.model.Notification;
import com.example.gestiondetareas.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserAndReadFalseOrderByCreatedAtDesc(User user);
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    Long countByUserAndReadFalse(User user);
}