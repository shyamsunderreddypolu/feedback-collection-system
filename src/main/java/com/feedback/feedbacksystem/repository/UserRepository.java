package com.feedback.feedbacksystem.repository;

import com.feedback.feedbacksystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByDepartmentId(Long departmentId);

    List<User> findByRoleId(Integer roleId);

    List<User> findByActiveTrueAndIsDeletedFalse();
}
