package com.example.gestiondetareas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El asunto es obligatorio")
    @Size(min = 3, max = 200, message = "El asunto debe tener entre 3 y 200 caracteres")
    @Column(nullable = false, length = 200)
    private String subject;

    @NotBlank(message = "El contenido del mensaje es obligatorio")
    @Size(min = 1, max = 2000, message = "El mensaje debe tener entre 1 y 2000 caracteres")
    @Column(nullable = false, length = 2000)
    private String content;

    @NotNull
    @Column(name = "is_read", nullable = false)
    private Boolean read = false;

    @Column(name = "sent_at", nullable = false, updatable = false)
    private LocalDateTime sentAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
    }

    // Constructors
    public Message() {}

    public Message(String subject, String content, User sender, User receiver) {
        this.subject = subject;
        this.content = content;
        this.sender = sender;
        this.receiver = receiver;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Boolean getRead() { return read; }
    public void setRead(Boolean read) { this.read = read; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }

    public User getReceiver() { return receiver; }
    public void setReceiver(User receiver) { this.receiver = receiver; }
}