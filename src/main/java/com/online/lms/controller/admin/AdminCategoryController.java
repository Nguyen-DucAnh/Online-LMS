package com.online.lms.controller.admin;

import com.online.lms.entity.Category;
import com.online.lms.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@org.springframework.context.annotation.Scope("singleton")
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public String list(Model model) {
        log.info("Hits GET /admin/categories");
        List<Category> categories = categoryService.findAll();
        model.addAttribute("categories", categories);
        model.addAttribute("currentPage", "categories");
        return "admin/categories/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        log.info("Hits GET /admin/categories/new");
        model.addAttribute("category", new Category());
        model.addAttribute("currentPage", "categories");
        return "admin/categories/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        log.info("Hits GET /admin/categories/{}/edit", id);
        Category category = categoryService.findById(id);
        if (category == null) {
            return "redirect:/admin/categories";
        }
        model.addAttribute("category", category);
        model.addAttribute("currentPage", "categories");
        return "admin/categories/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("category") Category category, RedirectAttributes ra) {
        log.info("POST /admin/categories/save - Name: {}", category.getName());
        categoryService.save(category);
        ra.addFlashAttribute("successMessage", "Đã lưu danh mục thành công!");
        return "redirect:/admin/categories";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        log.info("Hits POST /admin/categories/{}/delete", id);
        categoryService.delete(id);
        ra.addFlashAttribute("successMessage", "Đã xóa danh mục!");
        return "redirect:/admin/categories";
    }
}
