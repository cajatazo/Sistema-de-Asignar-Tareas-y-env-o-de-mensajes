package com.example.gestiondetareas.controller;

import com.example.gestiondetareas.model.User;
import com.example.gestiondetareas.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {
    
    @Autowired
    private UserService userService;
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userService.findUserByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
    
    @GetMapping("/profile")
    public String showProfile(Model model) {
        User currentUser = getCurrentUser();
        model.addAttribute("currentUser", currentUser);
        return "profile/view";
    }
    
    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String firstName,
                               @RequestParam String lastName,
                               RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        
        try {
            currentUser.setFirstName(firstName);
            currentUser.setLastName(lastName);
            userService.updateUser(currentUser.getId(), currentUser);
            redirectAttributes.addFlashAttribute("success", "Perfil actualizado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el perfil: " + e.getMessage());
        }
        
        return "redirect:/profile";
    }
}

