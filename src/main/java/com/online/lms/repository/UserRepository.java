package com.online.lms.repository;

import com.online.lms.entity.User;

import com.online.lms.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRoleIn(List<UserRole> roles);
    
    long countByRole(UserRole role);
}
