package com.online.lms.service;

import com.online.lms.dto.request.user.ChangePasswordRequestDTO;
import com.online.lms.dto.request.user.UpdateProfileRequestDTO;
import com.online.lms.dto.request.user.UserRequestDTO;
import com.online.lms.entity.User;
import com.online.lms.enums.UserRole;
import com.online.lms.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    List<User> getAllUsers();

    User getUserById(Long id);

    User getCurrentUser();

    User getCurrentUserProfile();

    void updateProfile(UpdateProfileRequestDTO request);

    void changePassword(ChangePasswordRequestDTO request);

    Page<User> searchUsers(UserRole role, UserStatus status, String keyword, Pageable pageable);

    void saveUserFromForm(User user);

    void deleteUser(Long id);

    void createNewUser(UserRequestDTO dto);

    void updateUser(Long id, UserRequestDTO dto);

    void toggleStatus(Long id);

    boolean existsByEmail(String email);
}