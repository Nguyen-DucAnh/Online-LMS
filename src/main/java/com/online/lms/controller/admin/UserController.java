package com.online.lms.controller.admin;

import com.online.lms.entity.User;
import com.online.lms.enums.UserRole;
import com.online.lms.enums.UserStatus;
import com.online.lms.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
public class UserController {

    @Autowired
    private UserService userService;

    // 1. Màn hình danh sách (Phân trang, Lọc, Tìm kiếm)
    @GetMapping
    public String listUsers(
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        PageRequest pageable = PageRequest.of(page, 10, Sort.by("id").descending());
        Page<User> userPage = userService.searchUsers(role, status, keyword, pageable);

        model.addAttribute("userPage", userPage);
        model.addAttribute("roles", UserRole.values());
        model.addAttribute("statuses", UserStatus.values());

        model.addAttribute("selectedRole", role);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("keyword", keyword);

        return "admin/user/user-list";
    }

    // 2. Màn hình thêm mới
    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", UserRole.values());
        model.addAttribute("statuses", UserStatus.values());
        return "admin/user/user-detail";
    }

    // 3. Màn hình chỉnh sửa
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("user", userService.getUserById(id));
        model.addAttribute("roles", UserRole.values());
        model.addAttribute("statuses", UserStatus.values());
        return "admin/user/user-detail";
    }

    // 4. Lưu dữ liệu
    @PostMapping("/save")
    public String saveUser(@Valid @ModelAttribute("user") User user,
                           BindingResult result, Model model) {

        // THÊM ĐOẠN NÀY: Kiểm tra trùng Email khi Thêm Mới User
        if (user.getId() == null && userService.existsByEmail(user.getEmail())) {
            // Đẩy lỗi vào ô email để Thymeleaf hiển thị ra màn hình
            result.rejectValue("email", "error.user", "This email address is already in use!");
        }

        // Nếu có lỗi (lỗi để trống, lỗi sai format, hoặc lỗi trùng email ở trên)
        if (result.hasErrors()) {
            model.addAttribute("roles", UserRole.values());
            model.addAttribute("statuses", UserStatus.values());
            return "admin/user/user-detail"; // Quay lại trang form cùng thông báo lỗi
        }

        // Nếu qua hết các vòng kiểm tra thì mới tiến hành lưu
        userService.saveUserFromForm(user);

        return "redirect:/admin/users";
    }


    @GetMapping("/toggle/{id}")
    public String toggleStatus(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user.getStatus() == UserStatus.ACTIVE) {
            user.setStatus(UserStatus.PENDING);
        } else {
            user.setStatus(UserStatus.ACTIVE);
        }
        userService.saveUserFromForm(user);
        return "redirect:/admin/users";
    }

    // 6. Xóa User
    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }
}