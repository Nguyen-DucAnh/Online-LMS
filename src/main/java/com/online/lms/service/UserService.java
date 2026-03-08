package com.online.lms.service;

import com.online.lms.dto.request.user.ChangePasswordRequestDTO;
import com.online.lms.dto.request.user.UpdateProfileRequestDTO;
import com.online.lms.entity.User;

public interface UserService {
    // Profile methods
    User getCurrentUser(); // Implementation should retrieve from SecurityContext
    User getCurrentUserProfile();
    void updateProfile(UpdateProfileRequestDTO request);
    void changePassword(ChangePasswordRequestDTO request);
}
