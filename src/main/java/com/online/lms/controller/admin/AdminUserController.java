package com.online.lms.controller.admin;

import com.online.lms.dto.request.user.UserRequestDTO;
import com.online.lms.entity.User;
import com.online.lms.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    // Trả về templates/admin/user/list.html
    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/user/list";
    }

    // Trả về templates/admin/user/form.html
    @GetMapping("/new")
    public String showCreate(Model model) {
        model.addAttribute("userDto", new UserRequestDTO());
        return "admin/user/form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("userDto") UserRequestDTO dto,
                         BindingResult result, RedirectAttributes ra) {
        if (result.hasErrors()) return "admin/user/form";
        try {
            userService.createNewUser(dto);
            ra.addFlashAttribute("message", "Tạo user và gửi mail thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "admin/user/form";
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/edit/{id}")
    public String showEdit(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id);
        UserRequestDTO dto = new UserRequestDTO();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAddress(user.getAddress());
        dto.setRole(user.getRole().name());
        dto.setStatus(user.getStatus().name());

        model.addAttribute("userDto", dto);
        return "admin/user/form"; // templates/admin/user/form.html
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("userDto") UserRequestDTO dto,
                         BindingResult result, RedirectAttributes ra) {
        if (result.hasErrors()) return "admin/user/form";
        try {
            userService.updateUser(id, dto);
            ra.addFlashAttribute("message", "Cập nhật thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            userService.deleteUser(id);
            ra.addFlashAttribute("message", "Xóa thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/toggle/{id}")
    public String toggleStatus(@PathVariable Long id) {
        userService.toggleStatus(id);
        return "redirect:/admin/users";
    }
}