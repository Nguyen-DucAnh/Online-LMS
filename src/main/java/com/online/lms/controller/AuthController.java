package com.online.lms.controller;

import com.online.lms.dto.request.auth.OtpRequestDTO;
import com.online.lms.dto.request.auth.RegisterRequestDTO;
import com.online.lms.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("registerRequest")) {
            model.addAttribute("registerRequest", new RegisterRequestDTO());
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("registerRequest") RegisterRequestDTO request,
            BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "auth/register";
        }
        try {
            authService.register(request);
            redirectAttributes.addFlashAttribute("message",
                    "Registration successful! Please check your email for OTP.");
            return "redirect:/verify-otp?email=" + request.getEmail();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@Valid @ModelAttribute("otpRequest") OtpRequestDTO request,
            BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors())
            return "auth/verify-otp";

        try {
            if (authService.verifyOtp(request.getEmail(), request.getOtp())) {
                redirectAttributes.addFlashAttribute("message", "Email verified! You can now login.");
                return "redirect:/login";
            }
            redirectAttributes.addFlashAttribute("error", "Invalid OTP");
            return "redirect:/verify-otp?email=" + request.getEmail();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/verify-otp?email=" + request.getEmail();
        }
    }

    @GetMapping("/resend-otp")
    public String resendOtp(@RequestParam("email") String email, RedirectAttributes redirectAttributes) {
        try {
            authService.resendOtp(email);
            redirectAttributes.addFlashAttribute("message", "OTP resent successfully to " + email);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to resend OTP: " + e.getMessage());
        }
        return "redirect:/verify-otp?email=" + email;
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(@RequestParam("email") String email,
            RedirectAttributes redirectAttributes) {
        try {
            authService.forgotPassword(email);
            redirectAttributes.addFlashAttribute("message",
                    "Nếu email tồn tại trong hệ thống, mật khẩu mới đã được gửi tới email của bạn.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/forgot-password";
    }

}
