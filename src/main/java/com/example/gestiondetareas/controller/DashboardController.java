package com.example.gestiondetareas.controller;

import com.example.gestiondetareas.model.Role;
import com.example.gestiondetareas.model.User;
import com.example.gestiondetareas.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private CourseService courseService;
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private MessageService messageService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private SubmissionService submissionService;
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userService.findUserByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
    
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        User currentUser = getCurrentUser();
        
        model.addAttribute("currentUser", currentUser);
        try {
            model.addAttribute("unreadMessageCount", messageService.getUnreadMessageCount(currentUser.getId()));
        } catch (Exception e) {
            model.addAttribute("unreadMessageCount", 0L);
        }
        try {
            model.addAttribute("unreadNotificationCount", notificationService.getUnreadNotificationCount(currentUser.getId()));
        } catch (Exception e) {
            model.addAttribute("unreadNotificationCount", 0L);
        }
        
        switch (currentUser.getRole()) {
            case ADMIN:
                return showAdminDashboard(model, currentUser);
            case TEACHER:
                return showTeacherDashboard(model, currentUser);
            case STUDENT:
                return showStudentDashboard(model, currentUser);
            default:
                return "redirect:/access-denied";
        }
    }
    
    private String showAdminDashboard(Model model, User user) {
        model.addAttribute("totalUsers", userService.findAllActiveUsers().size());
        model.addAttribute("totalCourses", courseService.findAllActiveCourses().size());
        model.addAttribute("totalTasks", taskService.findTasksByTeacher(user.getId()).size());
        model.addAttribute("studentCount", userService.findUsersByRole(Role.STUDENT).size());
        model.addAttribute("teacherCount", userService.findUsersByRole(Role.TEACHER).size());
        model.addAttribute("pendingTasks", 0);
        model.addAttribute("todayMessages", 0);
        return "dashboard/admin-dashboard";
    }
    
    private String showTeacherDashboard(Model model, User user) {
        model.addAttribute("courses", courseService.findCoursesByTeacher(user.getId()));
        model.addAttribute("tasks", taskService.findTasksByTeacher(user.getId()));
        model.addAttribute("pendingSubmissions", submissionService.findSubmissionsByTeacher(user.getId())
            .stream().filter(s -> s.getGrade() == null).count());
        return "dashboard/teacher-dashboard";
    }
    
    private String showStudentDashboard(Model model, User user) {
        model.addAttribute("courses", courseService.findCoursesByStudent(user.getId()));
        model.addAttribute("upcomingTasks", taskService.findUpcomingTasksByStudent(user.getId()));
        return "dashboard/student-dashboard";
    }
}
