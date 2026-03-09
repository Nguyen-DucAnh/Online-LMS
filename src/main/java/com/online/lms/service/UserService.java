package com.online.lms.service;

import com.online.lms.dto.request.user.ChangePasswordRequestDTO;
import com.online.lms.dto.request.user.UpdateProfileRequestDTO;
import com.online.lms.dto.request.user.UserRequestDTO; // Tạo thêm DTO này
import com.online.lms.entity.User;
import java.util.List;

public interface UserService {
    // Admin methods
    List<User> getAllUsers();
    User getUserById(Long id);
    void createNewUser(UserRequestDTO userDto);
    void updateUser(Long id, UserRequestDTO userDto);
    void toggleStatus(Long id);
    void deleteUser(Long id);

    // Profile methods (Giữ nguyên)
    User getCurrentUser();
    User getCurrentUserProfile();
    void updateProfile(UpdateProfileRequestDTO request);
    void changePassword(ChangePasswordRequestDTO request);
}