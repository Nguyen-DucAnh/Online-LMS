package com.online.lms.service.impl;


import com.online.lms.dto.UserCreateRequest;
import com.online.lms.dto.UserUpdateRequest;
import com.online.lms.entity.User;
import com.online.lms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {


    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + id));
    }

    @Override
    @Transactional
    public User createUser(UserCreateRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại trong hệ thống!");
        }


        String rawPassword = RandomStringUtils.randomAlphanumeric(8);


        User newUser = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(rawPassword))
                .role(request.getRole())
                .isActive(true)
                .build();

        User savedUser = userRepository.save(newUser);

        emailService.sendAccountCreationEmail(savedUser.getEmail(), savedUser.getFullName(), rawPassword);

        return savedUser;
    }

    @Override
    @Transactional
    public User updateUser(Long id, UserUpdateRequest request) {
        User existingUser = getUserById(id);

        existingUser.setFullName(request.getFullName());
        existingUser.setPhone(request.getPhone());
        existingUser.setAddress(request.getAddress());
        existingUser.setRole(request.getRole());
        existingUser.setActive(request.isActive());

        return userRepository.save(existingUser);
    }

    @Override
    @Transactional
    public void toggleUserStatus(Long id) {
        User user = getUserById(id);
        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);


        boolean hasEnrollments = enrollmentRepository.existsByUserId(id);
        boolean hasPayments = paymentRepository.existsByUserId(id);

        if (hasEnrollments || hasPayments) {
            throw new RuntimeException("Không thể xóa! Người dùng này đã có giao dịch (khóa học/thanh toán) trong hệ thống.");
        }

        userRepository.delete(user);
    }
}