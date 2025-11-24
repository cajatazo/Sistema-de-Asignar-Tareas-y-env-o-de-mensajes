package com.example.gestiondetareas.service;

import com.example.gestiondetareas.model.Role;
import com.example.gestiondetareas.model.User;
import com.example.gestiondetareas.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private UserService userService;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = new User("John", "Doe", "john@example.com", "password", Role.STUDENT);
        testUser.setId(1L);
    }
    
    @Test
    void findAllActiveUsers_ShouldReturnActiveUsers() {
        // Arrange
        List<User> expectedUsers = Arrays.asList(testUser);
        when(userRepository.findByActiveTrue()).thenReturn(expectedUsers);
        
        // Act
        List<User> result = userService.findAllActiveUsers();
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository).findByActiveTrue();
    }
    
    @Test
    void createUser_WithNewEmail_ShouldCreateUser() {
        // Arrange
        when(userRepository.existsByEmail(testUser.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(testUser.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // Act
        User result = userService.createUser(testUser);
        
        // Assert
        assertNotNull(result);
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(userRepository).existsByEmail(testUser.getEmail());
        verify(passwordEncoder).encode(testUser.getPassword());
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    void createUser_WithExistingEmail_ShouldThrowException() {
        // Arrange
        when(userRepository.existsByEmail(testUser.getEmail())).thenReturn(true);
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> userService.createUser(testUser));
        verify(userRepository).existsByEmail(testUser.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void findUserByEmail_WithValidEmail_ShouldReturnUser() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        
        // Act
        Optional<User> result = userService.findUserByEmail(testUser.getEmail());
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser.getEmail(), result.get().getEmail());
        verify(userRepository).findByEmail(testUser.getEmail());
    }
}