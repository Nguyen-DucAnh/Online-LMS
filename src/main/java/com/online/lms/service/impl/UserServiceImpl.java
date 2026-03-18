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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
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
    public Page<User> searchUsers(UserRole role, UserStatus status, String keyword, Pageable pageable) {
        return userRepository.searchUsers(role, status, keyword, pageable);
    }
    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public void saveUserFromForm(User user) {
        if (user.getId() == null) {
            // 1. TẠO MỚI: Sinh mật khẩu ngẫu nhiên (8 ký tự)
            String randomPassword = UUID.randomUUID().toString().substring(0, 8);
            user.setPassword(passwordEncoder.encode(randomPassword));

            // Lưu user vào DB
            userRepository.save(user);

            // Gửi Email thông báo
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(user.getEmail());
                message.setSubject("Welcome to OLMS - Your Account Has Been Created");
                message.setText("Hello " + user.getFullName() + ",\n\n"
                        + "Your account has been successfully created.\n"
                        + "Here are your login details:\n"
                        + "Email: " + user.getEmail() + "\n"
                        + "Password: " + randomPassword + "\n\n"
                        + "Please log in and change your password as soon as possible.\n"
                        + "Best regards,\nOLMS Admin Team");
                mailSender.send(message);
            } catch (Exception e) {
                System.out.println("Lỗi gửi mail: " + e.getMessage());
            }
        } else {

            User existingUser = getUserById(user.getId());
            user.setPassword(existingUser.getPassword());
            userRepository.save(user);
        }
    }


    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);

        // Kiểm tra xem User đã có dữ liệu giao dịch/khóa học chưa
        // Lưu ý: Nếu entity User của bạn map tên khác cho danh sách khóa học (ví dụ getEnrollments()), hãy sửa lại cho khớp
        if (user.getCourses() != null && !user.getCourses().isEmpty()) {
            throw new RuntimeException("Cannot delete user who already has transactions or courses!");
        }

        userRepository.delete(user);
    }


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

    @Override
    @Transactional
    public void createNewUser(UserRequestDTO dto) {
        User user = new User();
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setAddress(dto.getAddress());
        user.setRole(UserRole.valueOf(dto.getRole().toUpperCase()));
        user.setStatus(UserStatus.valueOf(dto.getStatus().toUpperCase()));

        // Sinh mật khẩu ngẫu nhiên
        String randomPassword = UUID.randomUUID().toString().substring(0, 8);
        user.setPassword(passwordEncoder.encode(randomPassword));

        userRepository.save(user);

        // Gửi mail
        try {
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setTo(user.getEmail());
            simpleMailMessage.setSubject("Welcome to OLMS - Your Account Has Been Created");
            simpleMailMessage.setText("Hello " + user.getFullName() + ",\n\n"
                    + "Your account has been successfully created.\n"
                    + "Here are your login details:\n"
                    + "Email: " + user.getEmail() + "\n"
                    + "Password: " + randomPassword + "\n\n"
                    + "Please log in and change your password as soon as possible.\n"
                    + "Best regards,\nOLMS Admin Team");
            mailSender.send(simpleMailMessage);
        } catch (Exception e) {
            log.error("Lỗi gửi mail: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public void updateUser(Long id, UserRequestDTO dto) {
        User user = getUserById(id);
        user.setFullName(dto.getFullName());
        user.setPhone(dto.getPhone());
        user.setAddress(dto.getAddress());
        user.setRole(UserRole.valueOf(dto.getRole().toUpperCase()));
        user.setStatus(UserStatus.valueOf(dto.getStatus().toUpperCase()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void toggleStatus(Long id) {
        User user = getUserById(id);
        if (user.getStatus() == UserStatus.ACTIVE) {
            user.setStatus(UserStatus.SUSPENDED);
        } else {
            user.setStatus(UserStatus.ACTIVE);
        }
        userRepository.save(user);
    }
}