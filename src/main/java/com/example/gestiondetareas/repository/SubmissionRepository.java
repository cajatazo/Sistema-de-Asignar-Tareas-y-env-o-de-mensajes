package com.example.gestiondetareas.repository;

import com.example.gestiondetareas.model.Submission;
import com.example.gestiondetareas.model.SubmissionStatus;
import com.example.gestiondetareas.model.Task;
import com.example.gestiondetareas.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByTaskAndStudent(Task task, User student);
    List<Submission> findByTask(Task task);
    List<Submission> findByStudent(User student);
    List<Submission> findByStatus(SubmissionStatus status);
    
    @Query("SELECT s FROM Submission s WHERE s.task = :task AND s.student = :student ORDER BY s.submittedAt DESC")
    List<Submission> findByTaskAndStudentOrderBySubmittedAtDesc(@Param("task") Task task, @Param("student") User student);
    
    @Query("SELECT s FROM Submission s WHERE s.task.course.teacher.id = :teacherId")
    List<Submission> findByTeacherId(@Param("teacherId") Long teacherId);
    
    @Query("SELECT COUNT(s) FROM Submission s WHERE s.task = :task")
    Long countByTask(@Param("task") Task task);
}