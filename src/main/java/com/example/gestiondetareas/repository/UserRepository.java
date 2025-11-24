package com.example.gestiondetareas.repository;

import com.example.gestiondetareas.model.Role;
import com.example.gestiondetareas.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRole(Role role);
    List<User> findByActiveTrue();
    List<User> findByRoleAndActiveTrue(Role role);
    
    @Query("SELECT u FROM User u WHERE u.role = 'STUDENT' AND u.active = true AND u NOT IN " +
           "(SELECT s FROM Course c JOIN c.students s WHERE c.id = :courseId)")
    List<User> findStudentsNotInCourse(@Param("courseId") Long courseId);
    
    boolean existsByEmail(String email);
}