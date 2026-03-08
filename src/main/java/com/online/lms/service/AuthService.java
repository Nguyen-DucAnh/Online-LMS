package com.online.lms.service;

import com.online.lms.dto.request.auth.RegisterRequestDTO;
import com.online.lms.entity.User;

public interface AuthService {
    User register(RegisterRequestDTO request);
    boolean verifyOtp(String email, String otp);
    void resendOtp(String email);
    void forgotPassword(String email);
}
