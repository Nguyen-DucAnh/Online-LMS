package com.online.lms.repository;

import com.online.lms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
}


@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByUserId(Long userId);
}

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    boolean existsByUserId(Long userId);
}
