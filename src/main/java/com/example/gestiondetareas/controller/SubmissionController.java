package com.example.gestiondetareas.controller;

import com.example.gestiondetareas.model.Submission;
import com.example.gestiondetareas.model.Task;
import com.example.gestiondetareas.model.User;
import com.example.gestiondetareas.service.SubmissionService;
import com.example.gestiondetareas.service.TaskService;
import com.example.gestiondetareas.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/submissions")
public class SubmissionController {

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private UserService userService;

    @Autowired
    private TaskService taskService;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userService.findUserByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    @GetMapping("/task/{taskId}")
    public String getSubmissionsByTask(@PathVariable Long taskId, Model model) {
        User currentUser = getCurrentUser();
        Task task = taskService.findTaskById(taskId);
        List<Submission> submissions = submissionService.findSubmissionsByTask(taskId);
        
        model.addAttribute("submissions", submissions);
        model.addAttribute("task", task);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("taskId", taskId);
        return "submissions/list";
    }

    @GetMapping("/submit/{taskId}")
    public String showSubmissionForm(@PathVariable Long taskId, Model model) {
        User currentUser = getCurrentUser();
        Task task = taskService.findTaskById(taskId);
        model.addAttribute("taskId", taskId);
        model.addAttribute("task", task);
        model.addAttribute("currentUser", currentUser);
        return "submissions/submit";
    }

    @PostMapping("/submit/{taskId}")
    public String submitTask(@PathVariable Long taskId,
                           @RequestParam String comment,
                           @RequestParam(required = false) MultipartFile file,
                           RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        
        try {
            submissionService.submitTask(taskId, currentUser.getId(), comment, file);
            redirectAttributes.addFlashAttribute("success", "Tarea entregada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al entregar la tarea: " + e.getMessage());
        }
        
        return "redirect:/tasks";
    }

    @GetMapping("/{submissionId}/grade")
    public String showGradeForm(@PathVariable Long submissionId, Model model) {
        User currentUser = getCurrentUser();
        Submission submission = submissionService.findSubmissionById(submissionId);
        
        model.addAttribute("submission", submission);
        model.addAttribute("currentUser", currentUser);
        return "submissions/grade";
    }

    @PostMapping("/{submissionId}/grade")
    public String gradeSubmission(@PathVariable Long submissionId,
                                @RequestParam Integer grade,
                                @RequestParam String feedback,
                                RedirectAttributes redirectAttributes) {
        try {
            submissionService.gradeSubmission(submissionId, grade, feedback);
            redirectAttributes.addFlashAttribute("success", "Tarea calificada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al calificar la tarea: " + e.getMessage());
        }
        
        return "redirect:/submissions/task/" + submissionService.findSubmissionById(submissionId).getTask().getId();
    }

    @GetMapping("/{submissionId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long submissionId) {
        try {
            byte[] fileContent = submissionService.getSubmissionFile(submissionId);
            Submission submission = submissionService.findSubmissionById(submissionId);
            
            ByteArrayResource resource = new ByteArrayResource(fileContent);
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + submission.getFileName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
                
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/my-submissions")
    public String getMySubmissions(Model model) {
        User currentUser = getCurrentUser();
        List<Submission> submissions = submissionService.findSubmissionsByStudent(currentUser.getId());
        
        model.addAttribute("submissions", submissions);
        model.addAttribute("currentUser", currentUser);
        return "submissions/my-submissions";
    }
}