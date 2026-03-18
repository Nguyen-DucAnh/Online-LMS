package com.online.lms.controller.admin;

import com.online.lms.constant.CourseViewNames;
import com.online.lms.dto.course.CourseFormDTO;
import com.online.lms.entity.User;
import com.online.lms.enums.CourseLevel;
import com.online.lms.enums.CourseStatus;
import com.online.lms.enums.UserRole;
import com.online.lms.repository.UserRepository;
import com.online.lms.service.CategoryService;
import com.online.lms.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@Scope("singleton")
@RequestMapping("/admin/courses")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class CourseController {

    private final CourseService courseService;
    private final CategoryService categoryService;
    private final UserRepository userRepository;

    @GetMapping
    public String list(Model model,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Long categoryId,
                       @RequestParam(required = false) String status,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Principal principal) {

        User currentUser = getCurrentUser(principal);
        Long instructorId = (currentUser.getRole() == UserRole.MANAGER) ? currentUser.getId() : null;

        CourseStatus courseStatus = (status != null && !status.isBlank())
                ? CourseStatus.valueOf(status) : null;

        model.addAttribute("courses", courseService.search(keyword, categoryId, courseStatus, instructorId,
                PageRequest.of(page, size)));
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("courseStatuses", CourseStatus.values());
        model.addAttribute("currentPage", "courses");
        return CourseViewNames.COURSE_LIST;
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("courseForm", new CourseFormDTO());
        populateFormModel(model);
        return CourseViewNames.COURSE_FORM;
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, Principal principal) {
        User currentUser = getCurrentUser(principal);
        CourseFormDTO formDTO = courseService.findFormById(id);

        if (currentUser.getRole() == UserRole.MANAGER && !currentUser.getId().equals(formDTO.getInstructorId())) {
            return "redirect:/admin/courses";
        }

        model.addAttribute("courseForm", formDTO);
        populateFormModel(model);
        return CourseViewNames.COURSE_FORM;
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("courseForm") CourseFormDTO dto,
                       BindingResult result, Model model,
                       RedirectAttributes redirectAttributes,
                       Principal principal) {
        User currentUser = getCurrentUser(principal);
        if (currentUser.getRole() == UserRole.MANAGER) {
            dto.setInstructorId(currentUser.getId());
            dto.setStatus(CourseStatus.UNPUBLISHED); // Managers create unpublished by default
            dto.setListedPrice(null); // Will be handled in service to keep original or 0
        }

        if (result.hasErrors()) {
            populateFormModel(model);
            return CourseViewNames.COURSE_FORM;
        }
        courseService.save(dto);
        redirectAttributes.addFlashAttribute("successMessage", "Tạo khóa học thành công!");
        return "redirect:/admin/courses";
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("courseForm") CourseFormDTO dto,
                         BindingResult result, Model model,
                         RedirectAttributes redirectAttributes,
                         Principal principal) {
        User currentUser = getCurrentUser(principal);
        CourseFormDTO existing = courseService.findFormById(id);

        if (currentUser.getRole() == UserRole.MANAGER && !currentUser.getId().equals(existing.getInstructorId())) {
            return "redirect:/admin/courses";
        }

        if (result.hasErrors()) {
            populateFormModel(model);
            return CourseViewNames.COURSE_FORM;
        }

        // Field restrictions for Managers are handled in CourseServiceImpl.update
        courseService.update(id, dto);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật khóa học thành công!");
        return "redirect:/admin/courses";
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes ra) {
        courseService.toggleStatus(id);
        ra.addFlashAttribute("successMessage", "Đã cập nhật trạng thái khóa học!");
        return "redirect:/admin/courses";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        courseService.deleteById(id);
        ra.addFlashAttribute("successMessage", "Đã xóa khóa học!");
        return "redirect:/admin/courses";
    }

    // ===== Private helpers =====

    private User getCurrentUser(Principal principal) {
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void populateFormModel(Model model) {
        model.addAttribute("categories", categoryService.findAllActive());
        model.addAttribute("levels", CourseLevel.values());
        model.addAttribute("courseStatuses", CourseStatus.values());
        model.addAttribute("instructors",
                userRepository.findByRoleIn(List.of(UserRole.ADMIN, UserRole.MANAGER)));
        model.addAttribute("currentPage", "courses");
    }
}
