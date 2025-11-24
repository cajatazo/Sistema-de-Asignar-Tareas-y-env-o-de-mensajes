package com.example.gestiondetareas.controller;

import com.example.gestiondetareas.model.Course;
import com.example.gestiondetareas.model.User;
import com.example.gestiondetareas.service.CourseService;
import com.example.gestiondetareas.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/courses")
public class CourseController {
    
    @Autowired
    private CourseService courseService;
    
    @Autowired
    private UserService userService;
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userService.findUserByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
    
    @GetMapping
    public String listCourses(Model model) {
        User currentUser = getCurrentUser();
        List<Course> courses;
        
        switch (currentUser.getRole()) {
            case ADMIN:
                courses = courseService.findAllActiveCourses();
                break;
            case TEACHER:
                courses = courseService.findCoursesByTeacher(currentUser.getId());
                break;
            case STUDENT:
                courses = courseService.findCoursesByStudent(currentUser.getId());
                break;
            default:
                courses = List.of();
        }
        
        model.addAttribute("courses", courses);
        model.addAttribute("currentUser", currentUser);
        return "courses/list";
    }
    
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        User currentUser = getCurrentUser();
        if (!currentUser.getRole().equals(com.example.gestiondetareas.model.Role.TEACHER)) {
            return "redirect:/access-denied";
        }
        
        model.addAttribute("course", new Course());
        return "courses/create";
    }
    
    @PostMapping("/create")
    public String createCourse(@Valid @ModelAttribute Course course, BindingResult result, 
                              Model model, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        
        if (result.hasErrors()) {
            model.addAttribute("course", course);
            return "courses/create";
        }
        
        try {
            courseService.createCourse(course, currentUser.getId());
            redirectAttributes.addFlashAttribute("success", "Curso creado exitosamente");
            return "redirect:/courses";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear el curso: " + e.getMessage());
            return "redirect:/courses/create";
        }
    }
    
    @GetMapping("/{id}")
    public String viewCourse(@PathVariable Long id, Model model) {
        Course course = courseService.findCourseById(id)
            .orElseThrow(() -> new RuntimeException("Curso no encontrado"));
        User currentUser = getCurrentUser();
        
        // Verificar permisos
        if (currentUser.getRole().equals(com.example.gestiondetareas.model.Role.STUDENT) && 
            !courseService.isStudentEnrolled(id, currentUser.getId())) {
            return "redirect:/access-denied";
        }
        
        model.addAttribute("course", course);
        model.addAttribute("currentUser", currentUser);
        return "courses/view";
    }
    
    @GetMapping("/{id}/enroll")
    public String enrollInCourse(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        
        if (!currentUser.getRole().equals(com.example.gestiondetareas.model.Role.STUDENT)) {
            return "redirect:/access-denied";
        }
        
        try {
            courseService.enrollStudent(id, currentUser.getId());
            redirectAttributes.addFlashAttribute("success", "Te has inscrito en el curso exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al inscribirse en el curso: " + e.getMessage());
        }
        
        return "redirect:/courses";
    }
    
    @GetMapping("/join")
    public String showJoinForm() {
        return "courses/join";
    }
    
    @PostMapping("/join")
    public String joinCourse(@RequestParam String code, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        
        if (!currentUser.getRole().equals(com.example.gestiondetareas.model.Role.STUDENT)) {
            return "redirect:/access-denied";
        }
        
        try {
            Course course = courseService.findCourseByCode(code)
                .orElseThrow(() -> new RuntimeException("Código de curso inválido"));
            
            courseService.enrollStudent(course.getId(), currentUser.getId());
            redirectAttributes.addFlashAttribute("success", "Te has unido al curso exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al unirse al curso: " + e.getMessage());
        }
        
        return "redirect:/courses";
    }
    
    @GetMapping("/{id}/students")
    public String manageStudents(@PathVariable Long id, Model model) {
        User currentUser = getCurrentUser();
        Course course = courseService.findCourseById(id)
            .orElseThrow(() -> new RuntimeException("Curso no encontrado"));
        
        // Solo el profesor del curso puede gestionar estudiantes
        if (!currentUser.getRole().equals(com.example.gestiondetareas.model.Role.TEACHER) || 
            !course.getTeacher().getId().equals(currentUser.getId())) {
            return "redirect:/access-denied";
        }
        
        // Obtener todos los estudiantes disponibles y los ya inscritos
        List<User> allStudents = userService.findAllActiveUsers().stream()
            .filter(u -> u.getRole().equals(com.example.gestiondetareas.model.Role.STUDENT))
            .toList();
        
        model.addAttribute("course", course);
        model.addAttribute("allStudents", allStudents);
        model.addAttribute("currentUser", currentUser);
        return "courses/students";
    }
    
    @PostMapping("/{id}/students/add")
    public String addStudent(@PathVariable Long id, @RequestParam Long studentId, 
                            RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        Course course = courseService.findCourseById(id)
            .orElseThrow(() -> new RuntimeException("Curso no encontrado"));
        
        if (!currentUser.getRole().equals(com.example.gestiondetareas.model.Role.TEACHER) || 
            !course.getTeacher().getId().equals(currentUser.getId())) {
            return "redirect:/access-denied";
        }
        
        try {
            courseService.enrollStudent(id, studentId);
            redirectAttributes.addFlashAttribute("success", "Estudiante agregado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al agregar estudiante: " + e.getMessage());
        }
        
        return "redirect:/courses/" + id + "/students";
    }
    
    @PostMapping("/{id}/students/remove")
    public String removeStudent(@PathVariable Long id, @RequestParam Long studentId, 
                               RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        Course course = courseService.findCourseById(id)
            .orElseThrow(() -> new RuntimeException("Curso no encontrado"));
        
        if (!currentUser.getRole().equals(com.example.gestiondetareas.model.Role.TEACHER) || 
            !course.getTeacher().getId().equals(currentUser.getId())) {
            return "redirect:/access-denied";
        }
        
        try {
            courseService.removeStudent(id, studentId);
            redirectAttributes.addFlashAttribute("success", "Estudiante removido exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al remover estudiante: " + e.getMessage());
        }
        
        return "redirect:/courses/" + id + "/students";
    }
}