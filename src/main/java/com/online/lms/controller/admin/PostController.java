package com.online.lms.controller.admin;

import com.online.lms.dto.post.PostFormDTO;
import com.online.lms.enums.PostStatus;
import com.online.lms.enums.UserRole;
import com.online.lms.service.BlogCategoryService;
import com.online.lms.service.PostService;
import com.online.lms.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/posts")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER','MARKETING')")
public class PostController {

    private final PostService postService;
    private final BlogCategoryService blogCategoryService;
    private final UserService userService;

    @GetMapping
    public String list(Model model,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Long categoryId,
                       @RequestParam(required = false) String status,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(defaultValue = "createdAt") String sortBy,
                       @RequestParam(defaultValue = "desc") String sortDir) {

        PostStatus postStatus = (status != null && !status.isBlank())
                ? PostStatus.valueOf(status)
                : null;

        Sort sort = Sort.by("asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);

        boolean isMarketing = userService.getCurrentUser().getRole() == UserRole.MARKETING;

        model.addAttribute("posts", isMarketing
                ? postService.searchAdminOwned(keyword, categoryId, postStatus, PageRequest.of(page, size, sort))
                : postService.searchAdmin(keyword, categoryId, postStatus, PageRequest.of(page, size, sort)));

        model.addAttribute("categories", blogCategoryService.findAll());
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("postStatuses", PostStatus.values());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("currentPage", "posts");
        return "admin/posts/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("postForm", new PostFormDTO());
        populateFormModel(model);
        return "admin/posts/form";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("postForm", postService.findFormById(id));
        populateFormModel(model);
        return "admin/posts/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("postForm") PostFormDTO dto,
                       BindingResult result,
                       Model model,
                       RedirectAttributes ra) {
        if (result.hasErrors()) {
            populateFormModel(model);
            return "admin/posts/form";
        }
        postService.create(dto);
        ra.addFlashAttribute("successMessage", "Tạo bài đăng thành công!");
        return "redirect:/admin/posts";
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("postForm") PostFormDTO dto,
                         BindingResult result,
                         Model model,
                         RedirectAttributes ra) {
        if (result.hasErrors()) {
            populateFormModel(model);
            return "admin/posts/form";
        }
        postService.update(id, dto);
        ra.addFlashAttribute("successMessage", "Cập nhật bài đăng thành công!");
        return "redirect:/admin/posts";
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam("value") String value,
                               RedirectAttributes ra) {
        PostStatus status = PostStatus.valueOf(value);
        postService.updateStatus(id, status);
        ra.addFlashAttribute("successMessage", "Đã cập nhật trạng thái bài đăng!");
        return "redirect:/admin/posts";
    }

    private void populateFormModel(Model model) {
        model.addAttribute("categories", blogCategoryService.findAllActive());
        model.addAttribute("postStatuses", PostStatus.values());
        model.addAttribute("currentPage", "posts");
    }
}
