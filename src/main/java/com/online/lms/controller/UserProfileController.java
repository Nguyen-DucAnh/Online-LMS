package com.online.lms.controller;

import com.online.lms.dto.request.user.ChangePasswordRequestDTO;
import com.online.lms.dto.request.user.UpdateProfileRequestDTO;
import com.online.lms.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserService userService;

    @GetMapping
    public String profilePage(Model model) {
        model.addAttribute("user", userService.getCurrentUserProfile());
        if (!model.containsAttribute("updateProfileRequest")) {
            UpdateProfileRequestDTO updateDto = new UpdateProfileRequestDTO();
            updateDto.setFullname(userService.getCurrentUser().getFullname());
            updateDto.setPhone(userService.getCurrentUser().getPhone());
            updateDto.setAddress(userService.getCurrentUser().getAddress());
            updateDto.setAvatar(userService.getCurrentUser().getAvatar());
            model.addAttribute("updateProfileRequest", updateDto);
        }
        if (!model.containsAttribute("changePasswordRequest")) {
            model.addAttribute("changePasswordRequest", new ChangePasswordRequestDTO());
        }
        return "user/profile";
    }

    @PostMapping("/update")
    public String updateProfile(@Valid @ModelAttribute("updateProfileRequest") UpdateProfileRequestDTO request,
                                BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.updateProfileRequest", result);
            redirectAttributes.addFlashAttribute("updateProfileRequest", request);
            return "redirect:/profile";
        }
        try {
            userService.updateProfile(request);
            redirectAttributes.addFlashAttribute("message", "Profile updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(@Valid @ModelAttribute("changePasswordRequest") ChangePasswordRequestDTO request,
                                 BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.changePasswordRequest", result);
            redirectAttributes.addFlashAttribute("changePasswordRequest", request);
            return "redirect:/profile";
        }
        try {
            userService.changePassword(request);
            redirectAttributes.addFlashAttribute("message", "Password changed successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error changing password: " + e.getMessage());
        }
        return "redirect:/profile";
    }
}
