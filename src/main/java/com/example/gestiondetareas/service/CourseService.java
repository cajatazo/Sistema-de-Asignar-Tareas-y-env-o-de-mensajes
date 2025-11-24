package com.example.gestiondetareas.service;

import com.example.gestiondetareas.model.Course;
import com.example.gestiondetareas.model.User;
import com.example.gestiondetareas.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CourseService {
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private UserService userService;
    
    public List<Course> findAllActiveCourses() {
        return courseRepository.findByActiveTrue();
    }
    
    public List<Course> findCoursesByTeacher(Long teacherId) {
        User teacher = userService.findUserById(teacherId)
            .orElseThrow(() -> new RuntimeException("Profesor no encontrado"));
        return courseRepository.findByTeacherAndActiveTrue(teacher);
    }
    
    public List<Course> findCoursesByStudent(Long studentId) {
        return courseRepository.findByStudentIdAndActiveTrue(studentId);
    }
    
    public Optional<Course> findCourseById(Long id) {
        return courseRepository.findById(id);
    }
    
    public Optional<Course> findCourseByCode(String code) {
        return courseRepository.findByCodeAndActiveTrue(code);
    }
    
    public Course createCourse(Course course, Long teacherId) {
        User teacher = userService.findUserById(teacherId)
            .orElseThrow(() -> new RuntimeException("Profesor no encontrado"));
        course.setTeacher(teacher);
        return courseRepository.save(course);
    }
    
    public Course updateCourse(Long id, Course courseDetails) {
        return courseRepository.findById(id).map(course -> {
            course.setName(courseDetails.getName());
            course.setDescription(courseDetails.getDescription());
            return courseRepository.save(course);
        }).orElseThrow(() -> new RuntimeException("Curso no encontrado"));
    }
    
    public void deleteCourse(Long id) {
        courseRepository.findById(id).ifPresent(course -> {
            course.setActive(false);
            courseRepository.save(course);
        });
    }
    
    public Course enrollStudent(Long courseId, Long studentId) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Curso no encontrado"));
        User student = userService.findUserById(studentId)
            .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));
        
        if (!course.getStudents().contains(student)) {
            course.getStudents().add(student);
            return courseRepository.save(course);
        }
        return course;
    }
    
    public Course removeStudent(Long courseId, Long studentId) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Curso no encontrado"));
        User student = userService.findUserById(studentId)
            .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));
        
        course.getStudents().remove(student);
        return courseRepository.save(course);
    }
    
    public boolean isStudentEnrolled(Long courseId, Long studentId) {
        return courseRepository.isStudentEnrolled(courseId, studentId);
    }
}