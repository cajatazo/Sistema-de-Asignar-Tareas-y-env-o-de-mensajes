package com.example.gestiondetareas.controller;

import com.example.gestiondetareas.model.User;
import com.example.gestiondetareas.service.NotificationService;
import com.example.gestiondetareas.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userService.findUserByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    @GetMapping
    public String getNotifications(Model model) {
        User currentUser = getCurrentUser();
        model.addAttribute("notifications", notificationService.getUserNotifications(currentUser.getId()));
        model.addAttribute("currentUser", currentUser);
        return "notifications/list";
    }

    @PostMapping("/{id}/read")
    public String markAsRead(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            notificationService.markAsRead(id);
            redirectAttributes.addFlashAttribute("success", "Notificación marcada como leída");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al marcar la notificación");
        }
        return "redirect:/notifications";
    }

    @PostMapping("/mark-all-read")
    public String markAllAsRead(RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        try {
            notificationService.markAllAsRead(currentUser.getId());
            redirectAttributes.addFlashAttribute("success", "Todas las notificaciones marcadas como leídas");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al marcar las notificaciones");
        }
        return "redirect:/notifications";
    }

    @GetMapping("/unread-count")
    @ResponseBody
    public Long getUnreadCount() {
        User currentUser = getCurrentUser();
        return notificationService.getUnreadNotificationCount(currentUser.getId());
    }
}