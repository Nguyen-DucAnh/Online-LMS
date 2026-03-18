package com.online.lms.controller;

import com.online.lms.dto.course.CourseListItemDTO;
import com.online.lms.enums.CourseLevel;
import com.online.lms.service.CategoryService;
import com.online.lms.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final CourseService courseService;
    private final CategoryService categoryService;

    @GetMapping("/")
    public String homepage(Model model) {
        model.addAttribute("categories", categoryService.findAllActive());
        model.addAttribute("levels", CourseLevel.values());
        return "home";
    }

    @GetMapping("/api/courses")
    @ResponseBody
    public Page<CourseListItemDTO> searchPublishedCourses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String level,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {

        CourseLevel courseLevel = null;
        if (level != null && !level.isBlank()) {
            try {
                courseLevel = CourseLevel.valueOf(level);
            } catch (IllegalArgumentException ignored) {
            }
        }

        Sort sortOrder = buildSort(sort);
        return courseService.searchPublished(keyword, categoryId, courseLevel,
                PageRequest.of(page, size, sortOrder));
    }

    private Sort buildSort(String sortParam) {
        return switch (sortParam) {
            case "price-asc" -> Sort.by(Sort.Direction.ASC, "salePrice");
            case "price-desc" -> Sort.by(Sort.Direction.DESC, "salePrice");
            case "title-asc" -> Sort.by(Sort.Direction.ASC, "title");
            case "title-desc" -> Sort.by(Sort.Direction.DESC, "title");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }
}
