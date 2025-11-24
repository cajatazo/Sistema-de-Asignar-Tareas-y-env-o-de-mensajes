package com.example.gestiondetareas.service;

import com.example.gestiondetareas.model.Course;
import com.example.gestiondetareas.model.User;
import com.example.gestiondetareas.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {
    
    @Mock
    private CourseRepository courseRepository;
    
    @Mock
    private UserService userService;
    
    @InjectMocks
    private CourseService courseService;
    
    private Course testCourse;
    private User testTeacher;
    
    @BeforeEach
    void setUp() {
        testTeacher = new User("Jane", "Smith", "jane@example.com", "password", com.example.gestiondetareas.model.Role.TEACHER);
        testTeacher.setId(1L);
        
        testCourse = new Course("Math 101", "Basic Mathematics", testTeacher);
        testCourse.setId(1L);
    }
    
    @Test
    void createCourse_WithValidTeacher_ShouldCreateCourse() {
        // Arrange
        when(userService.findUserById(testTeacher.getId())).thenReturn(Optional.of(testTeacher));
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);
        
        // Act
        Course result = courseService.createCourse(testCourse, testTeacher.getId());
        
        // Assert
        assertNotNull(result);
        assertEquals(testCourse.getName(), result.getName());
        assertEquals(testTeacher, result.getTeacher());
        verify(userService).findUserById(testTeacher.getId());
        verify(courseRepository).save(any(Course.class));
    }
    
    @Test
    void enrollStudent_WithValidData_ShouldEnrollStudent() {
        // Arrange
        User student = new User("John", "Doe", "john@example.com", "password", com.example.gestiondetareas.model.Role.STUDENT);
        student.setId(2L);
        
        when(courseRepository.findById(testCourse.getId())).thenReturn(Optional.of(testCourse));
        when(userService.findUserById(student.getId())).thenReturn(Optional.of(student));
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);
        
        // Act
        Course result = courseService.enrollStudent(testCourse.getId(), student.getId());
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getStudents().contains(student));
        verify(courseRepository).findById(testCourse.getId());
        verify(userService).findUserById(student.getId());
        verify(courseRepository).save(any(Course.class));
    }
}