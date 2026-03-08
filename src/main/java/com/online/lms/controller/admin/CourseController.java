package com.online.lms.controller.admin;

import com.online.lms.constant.CourseViewNames;
import com.online.lms.dto.course.CourseFormDTO;
import com.online.lms.entity.enums.CourseLevel;
import com.online.lms.entity.enums.CourseStatus;
import com.online.lms.entity.enums.UserRole;
import com.online.lms.repository.UserRepository;
import com.online.lms.service.CategoryService;
import com.online.lms.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

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
                       @RequestParam(required = false) Integer categoryId,
                       @RequestParam(required = false) String status,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size) {

        CourseStatus courseStatus = (status != null && !status.isBlank())
                ? CourseStatus.valueOf(status) : null;

        model.addAttribute("courses", courseService.search(keyword, categoryId, courseStatus,
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
    public String showEditForm(@PathVariable int id, Model model) {
        model.addAttribute("courseForm", courseService.findFormById(id));
        populateFormModel(model);
        return CourseViewNames.COURSE_FORM;
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("courseForm") CourseFormDTO dto,
                       BindingResult result, Model model,
                       RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            populateFormModel(model);
            return CourseViewNames.COURSE_FORM;
        }
        courseService.save(dto);
        redirectAttributes.addFlashAttribute("successMessage", "Tạo khóa học thành công!");
        return "redirect:/admin/courses";
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable int id,
                         @Valid @ModelAttribute("courseForm") CourseFormDTO dto,
                         BindingResult result, Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            populateFormModel(model);
            return CourseViewNames.COURSE_FORM;
        }
        courseService.update(id, dto);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật khóa học thành công!");
        return "redirect:/admin/courses";
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable int id, RedirectAttributes ra) {
        courseService.toggleStatus(id);
        ra.addFlashAttribute("successMessage", "Đã cập nhật trạng thái khóa học!");
        return "redirect:/admin/courses";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable int id, RedirectAttributes ra) {
        courseService.deleteById(id);
        ra.addFlashAttribute("successMessage", "Đã xóa khóa học!");
        return "redirect:/admin/courses";
    }

    // ===== Private helpers =====

    private void populateFormModel(Model model) {
        model.addAttribute("categories", categoryService.findAllActive());
        model.addAttribute("levels", CourseLevel.values());
        model.addAttribute("courseStatuses", CourseStatus.values());
        model.addAttribute("instructors",
                userRepository.findByRoleIn(List.of(UserRole.ADMIN, UserRole.MANAGER)));
        model.addAttribute("currentPage", "courses");
    }
}
