package com.online.lms.service.impl;

import com.online.lms.dto.request.user.ChangePasswordRequestDTO;
import com.online.lms.dto.request.user.UpdateProfileRequestDTO;
import com.online.lms.dto.request.user.UserRequestDTO;
import com.online.lms.entity.User;
import com.online.lms.enums.UserRole;
import com.online.lms.enums.UserStatus;
import com.online.lms.repository.UserRepository;
import com.online.lms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    @Transactional
    public void createNewUser(UserRequestDTO dto) {
        // Validation thủ công thay vì dùng Annotation tại Entity
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email đã tồn tại trên hệ thống!");
        }

        String rawPassword = UUID.randomUUID().toString().substring(0, 8);
        User user = User.builder()
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .role(UserRole.valueOf(dto.getRole()))
                .status(UserStatus.ACTIVE) // Đảm bảo Enum UserStatus có ACTIVE
                .password(passwordEncoder.encode(rawPassword))
                .build();

        userRepository.save(user);

        // Gửi mail
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("LMS Account Created");
        message.setText("Mật khẩu của bạn là: " + rawPassword);
        mailSender.send(message);
    }

    @Override
    @Transactional
    public void updateUser(Long id, UserRequestDTO dto) {
        User user = getUserById(id);
        user.setFullName(dto.getFullName());
        user.setPhone(dto.getPhone());
        user.setAddress(dto.getAddress());
        user.setRole(UserRole.valueOf(dto.getRole()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void toggleStatus(Long id) {
        User user = getUserById(id);
        // Kiểm tra đúng giá trị Enum của bạn (Ví dụ: ACTIVE/DEACTIVE)
        if (user.getStatus() == UserStatus.ACTIVE) {
            user.setStatus(UserStatus.PENDING);
        } else {
            user.setStatus(UserStatus.ACTIVE);
        }
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        // Logic: Chỉ user mới (không có giao dịch/khóa học) mới được xóa
        if (user.getInstructedCourses() != null && !user.getInstructedCourses().isEmpty()) {
            throw new RuntimeException("Không thể xóa người dùng đã có dữ liệu khóa học!");
        }
        userRepository.delete(user);
    }

    // --- Giữ nguyên các hàm cũ của bạn ---
    @Override
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public User getCurrentUserProfile() { return getCurrentUser(); }

    @Override
    @Transactional
    public void updateProfile(UpdateProfileRequestDTO request) {
        User user = getCurrentUser();
        user.setFullName(request.getFullname());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setAvatar(request.getAvatar());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequestDTO request) {
        User user = getCurrentUser();
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword()))
            throw new RuntimeException("Current password is incorrect");
        if (!request.getNewPassword().equals(request.getConfirmPassword()))
            throw new RuntimeException("Passwords do not match");
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}