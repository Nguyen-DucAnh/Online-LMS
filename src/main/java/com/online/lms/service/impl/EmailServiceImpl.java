package com.online.lms.service.impl;

import com.online.lms.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendOtpEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("OLMS System <your-email@gmail.com>");
        message.setTo(to);
        message.setSubject("OLMS - Verification OTP");
        message.setText("Hello,\n\nYour verification code is: " + otp + "\n\nPlease define this code to verify your account.\n\nThank you!");
        mailSender.send(message);
    }

    @Override
    public void sendNewPassword(String to, String newPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("OLMS System <your-email@gmail.com>");
        message.setTo(to);
        message.setSubject("OLMS - New Password");
        message.setText("Hello,\n\nYour new password is: " + newPassword + "\n\nPlease login and change your password immediately.\n\nThank you!");
        mailSender.send(message);
    }
}
