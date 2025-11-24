package com.example.gestiondetareas.repository;

import com.example.gestiondetareas.model.Course;
import com.example.gestiondetareas.model.Task;
import com.example.gestiondetareas.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByCourseAndStatus(Course course, TaskStatus status);
    List<Task> findByCourse(Course course);
    
    @Query("SELECT t FROM Task t WHERE t.course IN :courses AND t.dueDate > :now ORDER BY t.dueDate ASC")
    List<Task> findUpcomingTasks(@Param("courses") List<Course> courses, @Param("now") LocalDateTime now);
    
    @Query("SELECT t FROM Task t WHERE t.course.teacher.id = :teacherId")
    List<Task> findByTeacherId(@Param("teacherId") Long teacherId);
}