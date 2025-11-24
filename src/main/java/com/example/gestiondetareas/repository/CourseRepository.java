package com.example.gestiondetareas.repository;

import com.example.gestiondetareas.model.Course;
import com.example.gestiondetareas.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByActiveTrue();
    List<Course> findByTeacherAndActiveTrue(User teacher);
    
    @Query("SELECT c FROM Course c JOIN c.students s WHERE s.id = :studentId AND c.active = true")
    List<Course> findByStudentIdAndActiveTrue(@Param("studentId") Long studentId);
    
    Optional<Course> findByCodeAndActiveTrue(String code);
    
    @Query("SELECT COUNT(c) > 0 FROM Course c JOIN c.students s WHERE c.id = :courseId AND s.id = :studentId")
    boolean isStudentEnrolled(@Param("courseId") Long courseId, @Param("studentId") Long studentId);
}