package com.online.lms.controller.admin;

import com.online.lms.dto.chapter.ChapterDTO;
import com.online.lms.entity.Chapter;
import com.online.lms.entity.Course;
import com.online.lms.service.CourseContentService;
import com.online.lms.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/courses/{courseId}/chapters")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class ChapterDetailsController {

    private final CourseContentService contentService;
    private final CourseService courseService;

    @GetMapping("/new")
    public String showCreateForm(@PathVariable Long courseId, Model model) {
        Course course = courseService.findById(courseId);
        ChapterDTO dto = ChapterDTO.builder()
                .courseId(courseId)
                .orderIndex(contentService.findChaptersByCourse(courseId).size() + 1L)
                .build();
        model.addAttribute("course", course);
        model.addAttribute("chapter", dto);
        model.addAttribute("currentPage", "courses");
        return "admin/chapter-details";
    }

    @GetMapping("/{chapterId}/edit")
    public String showEditForm(@PathVariable Long courseId,
                               @PathVariable Long chapterId,
                               Model model) {
        Course course = courseService.findById(courseId);
        ChapterDTO chapter = contentService.findChapterById(chapterId);
        if (!chapter.getCourseId().equals(courseId)) {
            return "redirect:/admin/courses/" + courseId + "/content";
        }
        model.addAttribute("course", course);
        model.addAttribute("chapter", chapter);
        model.addAttribute("currentPage", "courses");
        return "admin/chapter-details";
    }

    @PostMapping("/new")
    public String createChapter(@PathVariable Long courseId,
                                @Valid @ModelAttribute("chapter") ChapterDTO dto,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes ra) {
        dto.setCourseId(courseId);
        if (bindingResult.hasErrors()) {
            model.addAttribute("course", courseService.findById(courseId));
            model.addAttribute("currentPage", "courses");
            return "admin/chapter-details";
        }
        contentService.saveChapter(dto);
        ra.addFlashAttribute("successMessage", "Đã tạo chương thành công!");
        return "redirect:/admin/courses/" + courseId + "/content";
    }

    @PostMapping("/{chapterId}/edit")
    public String updateChapter(@PathVariable Long courseId,
                                @PathVariable Long chapterId,
                                @Valid @ModelAttribute("chapter") ChapterDTO dto,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes ra) {
        dto.setId(chapterId);
        dto.setCourseId(courseId);
        if (bindingResult.hasErrors()) {
            model.addAttribute("course", courseService.findById(courseId));
            model.addAttribute("currentPage", "courses");
            return "admin/chapter-details";
        }
        contentService.saveChapter(dto);
        ra.addFlashAttribute("successMessage", "Đã cập nhật chương!");
        return "redirect:/admin/courses/" + courseId + "/content";
    }
}
