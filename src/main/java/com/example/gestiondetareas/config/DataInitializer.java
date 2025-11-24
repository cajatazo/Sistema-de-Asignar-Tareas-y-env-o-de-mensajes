package com.example.gestiondetareas.config;

import com.example.gestiondetareas.model.User;
import com.example.gestiondetareas.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Actualizar contraseñas de usuarios existentes si tienen hashes inválidos
        updateUserPasswords();
    }
    
    private void updateUserPasswords() {
        // Lista de usuarios a actualizar
        String[] emails = {
            "admin@sistema.com",
            "maria.garcia@escuela.com",
            "carlos.lopez@escuela.com",
            "ana.martinez@estudiante.com",
            "pedro.rodriguez@estudiante.com",
            "laura.hernandez@estudiante.com",
            "jescampomanesza@uch.pe"
        };
        
        for (String email : emails) {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                // Verificar si el hash es inválido (contiene "ABCDEFGHIJKLMNOPQRSTUVWXYZ")
                if (user.getPassword().contains("ABCDEFGHIJKLMNOPQRSTUVWXYZ")) {
                    user.setPassword(passwordEncoder.encode("123456"));
                    userRepository.save(user);
                    System.out.println("Contraseña actualizada para: " + email);
                } else {
                    // Si el hash parece válido pero queremos asegurarnos, verificamos
                    // Si no puede verificar con "123456", lo actualizamos
                    if (!passwordEncoder.matches("123456", user.getPassword())) {
                        user.setPassword(passwordEncoder.encode("123456"));
                        userRepository.save(user);
                        System.out.println("Contraseña actualizada para: " + email);
                    }
                }
            }
        }
    }
}

