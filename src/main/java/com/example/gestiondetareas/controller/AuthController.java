package com.example.gestiondetareas.controller;

import com.example.gestiondetareas.dto.UserRegistrationDTO;
import com.example.gestiondetareas.model.Role;
import com.example.gestiondetareas.model.User;
import com.example.gestiondetareas.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }
    
    @GetMapping("/login")
    public String showLoginForm() {
        return "auth/login";
    }
    
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserRegistrationDTO());
        model.addAttribute("roles", Role.values());
        return "auth/register";
    }
    
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserRegistrationDTO userDTO, 
                              BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("roles", Role.values());
            return "auth/register";
        }
        
        if (userService.existsByEmail(userDTO.getEmail())) {
            result.rejectValue("email", "error.user", "El email ya está registrado");
            model.addAttribute("roles", Role.values());
            return "auth/register";
        }
        
        try {
            User user = new User(userDTO.getFirstName(), userDTO.getLastName(), 
                               userDTO.getEmail(), userDTO.getPassword(), userDTO.getRole());
            userService.createUser(user);
            
            redirectAttributes.addFlashAttribute("success", "Usuario registrado exitosamente. Puede iniciar sesión.");
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", "Error al registrar el usuario: " + e.getMessage());
            model.addAttribute("roles", Role.values());
            return "auth/register";
        }
    }
    
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/access-denied";
    }
}