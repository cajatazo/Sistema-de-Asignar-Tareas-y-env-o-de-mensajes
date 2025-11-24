package com.example.gestiondetareas.service;

import com.example.gestiondetareas.model.*;
import com.example.gestiondetareas.repository.SubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SubmissionService {
    
    @Autowired
    private SubmissionRepository submissionRepository;
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private NotificationService notificationService;
    
    private final String UPLOAD_DIR = "uploads/submissions/";
    
    public List<Submission> findSubmissionsByTask(Long taskId) {
        Task task = taskService.findTaskById(taskId);
        return submissionRepository.findByTask(task);
    }
    
    public List<Submission> findSubmissionsByStudent(Long studentId) {
        User student = userService.findUserById(studentId)
            .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));
        return submissionRepository.findByStudent(student);
    }
    
    public List<Submission> findSubmissionsByTeacher(Long teacherId) {
        return submissionRepository.findByTeacherId(teacherId);
    }
    
    public Optional<Submission> findSubmissionByTaskAndStudent(Long taskId, Long studentId) {
        Task task = taskService.findTaskById(taskId);
        User student = userService.findUserById(studentId)
            .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));
        List<Submission> submissions = submissionRepository.findByTaskAndStudentOrderBySubmittedAtDesc(task, student);
        return submissions.isEmpty() ? Optional.empty() : Optional.of(submissions.get(0));
    }
    
    public Submission submitTask(Long taskId, Long studentId, String comment, MultipartFile file) {
        Task task = taskService.findTaskById(taskId);
        User student = userService.findUserById(studentId)
            .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));
        
        // Verificar que el estudiante esté inscrito en el curso
        if (!task.getCourse().getStudents().contains(student)) {
            throw new RuntimeException("El estudiante no está inscrito en este curso");
        }
        
        String filePath = null;
        String fileName = null;
        
        if (file != null && !file.isEmpty()) {
            try {
                Files.createDirectories(Paths.get(UPLOAD_DIR));
                fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                filePath = UPLOAD_DIR + fileName;
                Files.write(Paths.get(filePath), file.getBytes());
            } catch (IOException e) {
                throw new RuntimeException("Error al guardar el archivo", e);
            }
        }
        
        Submission submission = new Submission(comment, filePath, fileName, task, student);
        Submission savedSubmission = submissionRepository.save(submission);
        
        // Notificar al profesor
        notificationService.createNotification(
            "Nueva Entrega de Tarea",
            "El estudiante " + student.getFullName() + " ha entregado la tarea: " + task.getTitle(),
            NotificationType.TASK_SUBMITTED,
            task.getCourse().getTeacher()
        );
        
        return savedSubmission;
    }
    
    public Submission gradeSubmission(Long submissionId, Integer grade, String feedback) {
        return submissionRepository.findById(submissionId).map(submission -> {
            submission.setGrade(grade);
            submission.setFeedback(feedback);
            submission.setStatus(SubmissionStatus.GRADED);
            
            Submission savedSubmission = submissionRepository.save(submission);
            
            // Notificar al estudiante
            notificationService.createNotification(
                "Tarea Calificada",
                "Tu tarea '" + submission.getTask().getTitle() + "' ha sido calificada: " + grade + " puntos",
                NotificationType.TASK_GRADED,
                submission.getStudent()
            );
            
            return savedSubmission;
        }).orElseThrow(() -> new RuntimeException("Entrega no encontrada"));
    }
    
    public byte[] getSubmissionFile(Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
            .orElseThrow(() -> new RuntimeException("Entrega no encontrada"));
        
        if (submission.getFilePath() == null) {
            throw new RuntimeException("Archivo no encontrado");
        }
        
        try {
            Path path = Paths.get(submission.getFilePath());
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el archivo", e);
        }
    }
    
    public Submission findSubmissionById(Long submissionId) {
        return submissionRepository.findById(submissionId)
            .orElseThrow(() -> new RuntimeException("Entrega no encontrada"));
    }
    
    public Long getSubmissionCountByTask(Long taskId) {
        Task task = taskService.findTaskById(taskId);
        return submissionRepository.countByTask(task);
    }
    
    public Long getSubmissionCountByTask(Task task) {
        return submissionRepository.countByTask(task);
    }
}