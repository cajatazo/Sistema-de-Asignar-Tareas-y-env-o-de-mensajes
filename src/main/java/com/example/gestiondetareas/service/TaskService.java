package com.example.gestiondetareas.service;

import com.example.gestiondetareas.model.*;
import com.example.gestiondetareas.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class TaskService {
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private CourseService courseService;
    
    @Autowired
    private NotificationService notificationService;
    
    public List<Task> findTasksByCourse(Long courseId) {
        Course course = courseService.findCourseById(courseId)
            .orElseThrow(() -> new RuntimeException("Curso no encontrado"));
        return taskRepository.findByCourse(course);
    }
    
    public List<Task> findUpcomingTasksByStudent(Long studentId) {
        List<Course> courses = courseService.findCoursesByStudent(studentId);
        return taskRepository.findUpcomingTasks(courses, LocalDateTime.now());
    }
    
    public List<Task> findTasksByTeacher(Long teacherId) {
        return taskRepository.findByTeacherId(teacherId);
    }
    
    public Task findTaskById(Long id) {
        return taskRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
    }
    
    public Task createTask(Task task, Long courseId) {
        Course course = courseService.findCourseById(courseId)
            .orElseThrow(() -> new RuntimeException("Curso no encontrado"));
        task.setCourse(course);
        
        Task savedTask = taskRepository.save(task);
        
        // Notificar a todos los estudiantes del curso
        for (User student : course.getStudents()) {
            notificationService.createNotification(
                "Nueva Tarea Asignada",
                "Se ha asignado una nueva tarea: " + task.getTitle(),
                NotificationType.TASK_ASSIGNED,
                student
            );
        }
        
        return savedTask;
    }
    
    public Task updateTask(Long id, Task taskDetails) {
        return taskRepository.findById(id).map(task -> {
            task.setTitle(taskDetails.getTitle());
            task.setDescription(taskDetails.getDescription());
            task.setDueDate(taskDetails.getDueDate());
            task.setPoints(taskDetails.getPoints());
            task.setStatus(taskDetails.getStatus());
            return taskRepository.save(task);
        }).orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
    }
    
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }
}