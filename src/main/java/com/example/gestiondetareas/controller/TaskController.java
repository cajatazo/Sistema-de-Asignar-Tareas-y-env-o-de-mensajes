package com.example.gestiondetareas.controller;

import com.example.gestiondetareas.model.*;
import com.example.gestiondetareas.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/tasks")
public class TaskController {
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private CourseService courseService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private SubmissionService submissionService;
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userService.findUserByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
    
    @GetMapping
    public String listTasks(Model model) {
        User currentUser = getCurrentUser();
        List<Task> tasks;
        List<Course> courses;
        
        switch (currentUser.getRole()) {
            case ADMIN:
            case TEACHER:
                tasks = taskService.findTasksByTeacher(currentUser.getId());
                courses = courseService.findCoursesByTeacher(currentUser.getId());
                break;
            case STUDENT:
                tasks = taskService.findUpcomingTasksByStudent(currentUser.getId());
                courses = courseService.findCoursesByStudent(currentUser.getId());
                break;
            default:
                tasks = List.of();
                courses = List.of();
        }
        
        // Preparar datos adicionales para la vista
        Map<Long, String> submissionStatusMap = new HashMap<>();
        Map<Long, Long> submissionCountMap = new HashMap<>();
        
        if (currentUser.getRole() == Role.STUDENT) {
            // Para estudiantes: estado de sus entregas
            for (Task task : tasks) {
                Optional<Submission> submission = submissionService.findSubmissionByTaskAndStudent(task.getId(), currentUser.getId());
                submissionStatusMap.put(task.getId(), 
                    submission.map(s -> s.getStatus().toString()).orElse(null));
            }
        } else {
            // Para profesores: conteo de entregas por tarea
            for (Task task : tasks) {
                Long count = submissionService.getSubmissionCountByTask(task.getId());
                submissionCountMap.put(task.getId(), count);
            }
        }
        
        model.addAttribute("tasks", tasks);
        model.addAttribute("courses", courses);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("submissionStatusMap", submissionStatusMap);
        model.addAttribute("submissionCountMap", submissionCountMap);
        return "tasks/list";
    }
    
    @GetMapping("/create")
    public String showCreateForm(@RequestParam(required = false) Long courseId, Model model) {
        User currentUser = getCurrentUser();
        if (!currentUser.getRole().equals(Role.TEACHER)) {
            return "redirect:/access-denied";
        }
        
        Task task = new Task();
        if (courseId != null) {
            Course course = courseService.findCourseById(courseId)
                .orElseThrow(() -> new RuntimeException("Curso no encontrado"));
            task.setCourse(course);
        }
        
        List<Course> courses = courseService.findCoursesByTeacher(currentUser.getId());
        
        model.addAttribute("task", task);
        model.addAttribute("courses", courses);
        model.addAttribute("minDate", LocalDateTime.now().plusDays(1));
        return "tasks/create";
    }
    
    @PostMapping("/create")
    public String createTask(@Valid @ModelAttribute Task task, BindingResult result, 
                            @RequestParam(required = false) Long courseId, Model model,
                            RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        
        if (result.hasErrors()) {
            List<Course> courses = courseService.findCoursesByTeacher(currentUser.getId());
            model.addAttribute("courses", courses);
            model.addAttribute("minDate", LocalDateTime.now().plusDays(1));
            return "tasks/create";
        }
        
        try {
            // Obtener courseId del parámetro o del objeto task
            Long finalCourseId = courseId;
            if (finalCourseId == null && task.getCourse() != null) {
                finalCourseId = task.getCourse().getId();
            }
            
            if (finalCourseId == null) {
                redirectAttributes.addFlashAttribute("error", "Debes seleccionar un curso");
                return "redirect:/tasks/create";
            }
            
            taskService.createTask(task, finalCourseId);
            redirectAttributes.addFlashAttribute("success", "Tarea creada exitosamente");
            return "redirect:/tasks";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear la tarea: " + e.getMessage());
            return "redirect:/tasks/create";
        }
    }
    
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        User currentUser = getCurrentUser();
        if (!currentUser.getRole().equals(Role.TEACHER)) {
            return "redirect:/access-denied";
        }
        
        Task task = taskService.findTaskById(id);
        List<Course> courses = courseService.findCoursesByTeacher(currentUser.getId());
        
        model.addAttribute("task", task);
        model.addAttribute("courses", courses);
        model.addAttribute("minDate", LocalDateTime.now().plusDays(1));
        return "tasks/edit";
    }
    
    @GetMapping("/{id}")
    public String viewTask(@PathVariable Long id, Model model) {
        Task task = taskService.findTaskById(id);
        User currentUser = getCurrentUser();
        
        // Verificar permisos
        if (currentUser.getRole().equals(Role.STUDENT) && 
            !courseService.isStudentEnrolled(task.getCourse().getId(), currentUser.getId())) {
            return "redirect:/access-denied";
        }
        
        // Para estudiantes: obtener su entrega si existe
        Submission mySubmission = null;
        if (currentUser.getRole() == Role.STUDENT) {
            Optional<Submission> submission = submissionService.findSubmissionByTaskAndStudent(id, currentUser.getId());
            mySubmission = submission.orElse(null);
        }
        
        // Para profesores: estadísticas de entregas
        Long submissionCount = 0L;
        Long pendingGradingCount = 0L;
        if (currentUser.getRole() == Role.TEACHER) {
            submissionCount = submissionService.getSubmissionCountByTask(id);
            pendingGradingCount = submissionService.findSubmissionsByTask(id)
                .stream()
                .filter(s -> s.getGrade() == null)
                .count();
        }
        
        model.addAttribute("task", task);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("mySubmission", mySubmission);
        model.addAttribute("submissionCount", submissionCount);
        model.addAttribute("pendingGradingCount", pendingGradingCount);
        return "tasks/view";
    }
    
    @PostMapping("/{id}/edit")
    public String updateTask(@PathVariable Long id, @Valid @ModelAttribute Task task, 
                           BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        
        if (result.hasErrors()) {
            List<Course> courses = courseService.findCoursesByTeacher(currentUser.getId());
            model.addAttribute("courses", courses);
            model.addAttribute("minDate", LocalDateTime.now().plusDays(1));
            model.addAttribute("task", task);
            return "tasks/edit";
        }
        
        try {
            taskService.updateTask(id, task);
            redirectAttributes.addFlashAttribute("success", "Tarea actualizada exitosamente");
            return "redirect:/tasks/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar la tarea: " + e.getMessage());
            return "redirect:/tasks/" + id + "/edit";
        }
    }
    
    @PostMapping("/{id}/delete")
    public String deleteTask(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        if (!currentUser.getRole().equals(Role.TEACHER)) {
            return "redirect:/access-denied";
        }
        
        try {
            taskService.deleteTask(id);
            redirectAttributes.addFlashAttribute("success", "Tarea eliminada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar la tarea: " + e.getMessage());
        }
        
        return "redirect:/tasks";
    }
}