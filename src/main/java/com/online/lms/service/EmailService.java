package com.online.lms.service;

public interface EmailService {
    void sendOtpEmail(String to, String otp);
    void sendNewPassword(String to, String newPassword);
}
