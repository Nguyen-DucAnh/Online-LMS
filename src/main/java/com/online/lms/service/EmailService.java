package com.online.lms.service;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

    public void sendAccountCreationEmail(String email, String fullName, String rawPassword) {
        System.out.println("Đã gửi email tới: " + email);
        System.out.println("Xin chào " + fullName + ", mật khẩu hệ thống của bạn là: " + rawPassword);
    }
}
