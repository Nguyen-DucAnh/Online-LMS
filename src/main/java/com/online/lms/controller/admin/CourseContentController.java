package com.online.lms.controller.admin;

import com.online.lms.dto.chapter.ChapterDTO;
import com.online.lms.entity.Course;
import com.online.lms.enums.LessonType;
import com.online.lms.exceptions.ResourceNotFoundException;
import com.online.lms.service.CourseContentService;
import com.online.lms.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@Scope("singleton")
@RequestMapping("/admin/courses/{courseId}")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class CourseContentController {

    private final CourseService courseService;
    private final CourseContentService contentService;

    @GetMapping("/content")
    public String content(@PathVariable Long courseId, Model model) {
        Course course;
        try {
            course = courseService.findById(courseId);
        } catch (ResourceNotFoundException e) {
            return "redirect:/admin/courses";
        }
        List<ChapterDTO> chapters = contentService.findChaptersByCourse(courseId);
        long totalLessons = chapters.stream()
                .mapToLong(ch -> ch.getLessons().size())
                .sum();

        model.addAttribute("course", course);
        model.addAttribute("courseId", courseId);
        model.addAttribute("chapters", chapters);
        model.addAttribute("totalLessons", totalLessons);
        model.addAttribute("lessonTypes", LessonType.values());
        model.addAttribute("currentPage", "courses");
        return "admin/courses/content";
    }

    @PostMapping("/chapters/{chapterId}/delete")
    public String deleteChapter(@PathVariable Long courseId,
                                @PathVariable Long chapterId,
                                RedirectAttributes ra) {
        contentService.deleteChapter(chapterId);
        ra.addFlashAttribute("successMessage", "Đã xóa chương!");
        return "redirect:/admin/courses/" + courseId + "/content";
    }

    @PostMapping("/chapters/{chapterId}/lessons/{lessonId}/toggle")
    public String toggleLesson(@PathVariable Long courseId,
                               @PathVariable Long chapterId,
                               @PathVariable Long lessonId) {
        contentService.toggleLessonStatus(lessonId);
        return "redirect:/admin/courses/" + courseId + "/content";
    }

    @PostMapping("/chapters/{chapterId}/lessons/{lessonId}/delete")
    public String deleteLesson(@PathVariable Long courseId,
                               @PathVariable Long chapterId,
                               @PathVariable Long lessonId,
                               RedirectAttributes ra) {
        contentService.deleteLesson(lessonId);
        ra.addFlashAttribute("successMessage", "Đã xóa bài học!");
        return "redirect:/admin/courses/" + courseId + "/content";
    }
}
