package com.example.gestiondetareas.controller;

import com.example.gestiondetareas.model.User;
import com.example.gestiondetareas.service.MessageService;
import com.example.gestiondetareas.service.UserService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/messages")
public class MessageController {
    
    @Autowired
    private MessageService messageService;
    
    @Autowired
    private UserService userService;
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userService.findUserByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
    
    @GetMapping
    public String listMessages(Model model) {
        User currentUser = getCurrentUser();
        
        model.addAttribute("receivedMessages", messageService.getReceivedMessages(currentUser.getId()));
        model.addAttribute("sentMessages", messageService.getSentMessages(currentUser.getId()));
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("unreadMessageCount", messageService.getUnreadMessageCount(currentUser.getId()));
        return "messages/list";
    }
    
    @GetMapping("/sent")
    public String listSentMessages(Model model) {
        User currentUser = getCurrentUser();
        
        model.addAttribute("receivedMessages", List.of());
        model.addAttribute("sentMessages", messageService.getSentMessages(currentUser.getId()));
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("unreadMessageCount", messageService.getUnreadMessageCount(currentUser.getId()));
        model.addAttribute("showSentOnly", true);
        return "messages/list";
    }
    
    @GetMapping("/compose")
    public String showComposeForm(@RequestParam(required = false) Long to, Model model) {
        User currentUser = getCurrentUser();
        
        model.addAttribute("users", userService.findAllActiveUsers());
        model.addAttribute("currentUser", currentUser);
        if (to != null) {
            model.addAttribute("recipientId", to);
        }
        return "messages/compose";
    }
    
    @PostMapping("/send")
    public String sendMessage(@RequestParam String subject, @RequestParam String content,
                            @RequestParam Long receiverId, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        
        try {
            messageService.sendMessage(subject, content, currentUser.getId(), receiverId);
            redirectAttributes.addFlashAttribute("success", "Mensaje enviado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al enviar el mensaje: " + e.getMessage());
        }
        
        return "redirect:/messages";
    }
    
    @GetMapping("/{id}")
    public String viewMessage(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        
        try {
            com.example.gestiondetareas.model.Message message = messageService.getMessageById(id);
            
            // Verificar que el usuario sea el remitente o receptor del mensaje
            if (!message.getSender().getId().equals(currentUser.getId()) && 
                !message.getReceiver().getId().equals(currentUser.getId())) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para ver este mensaje");
                return "redirect:/messages";
            }
            
            // Si el usuario es el receptor y el mensaje no está leído, marcarlo como leído
            if (message.getReceiver().getId().equals(currentUser.getId()) && !message.getRead()) {
                messageService.markAsRead(id);
                message.setRead(true);
            }
            
            model.addAttribute("message", message);
            model.addAttribute("currentUser", currentUser);
            return "messages/view";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cargar el mensaje: " + e.getMessage());
            return "redirect:/messages";
        }
    }
    
    @PostMapping("/{id}/read")
    public String markAsRead(@PathVariable Long id) {
        messageService.markAsRead(id);
        return "redirect:/messages";
    }
}