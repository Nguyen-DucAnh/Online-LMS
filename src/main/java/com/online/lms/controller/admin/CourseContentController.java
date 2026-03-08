package com.online.lms.controller.admin;

import com.online.lms.constant.CourseViewNames;
import com.online.lms.dto.chapter.ChapterDTO;
import com.online.lms.dto.chapter.LessonDTO;
import com.online.lms.enums.LessonType;
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
        model.addAttribute("course", courseService.findById(courseId));
        model.addAttribute("chapters", contentService.findChaptersByCourse(courseId));
        model.addAttribute("newChapter", ChapterDTO.builder().courseId(courseId).build());
        model.addAttribute("newLesson", new LessonDTO());
        model.addAttribute("lessonTypes", LessonType.values());
        model.addAttribute("currentPage", "courses");
        return CourseViewNames.COURSE_CONTENT;
    }

    @PostMapping("/chapters/save")
    public String saveChapter(@PathVariable Long courseId,
                              @ModelAttribute ChapterDTO dto,
                              RedirectAttributes ra) {
        dto.setCourseId(courseId);
        contentService.saveChapter(dto);
        ra.addFlashAttribute("successMessage", "Lưu chương thành công!");
        return "redirect:/admin/courses/" + courseId + "/content";
    }

    @PostMapping("/chapters/{chapterId}/delete")
    public String deleteChapter(@PathVariable Long courseId,
                                @PathVariable Long chapterId,
                                RedirectAttributes ra) {
        contentService.deleteChapter(chapterId);
        ra.addFlashAttribute("successMessage", "Đã xóa chương!");
        return "redirect:/admin/courses/" + courseId + "/content";
    }

    @PostMapping("/chapters/{chapterId}/lessons/save")
    public String saveLesson(@PathVariable Long courseId,
                             @PathVariable Long chapterId,
                             @ModelAttribute LessonDTO dto,
                             RedirectAttributes ra) {
        dto.setChapterId(chapterId);
        contentService.saveLesson(dto);
        ra.addFlashAttribute("successMessage", "Lưu bài học thành công!");
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
