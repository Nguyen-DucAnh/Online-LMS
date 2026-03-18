package com.online.lms.controller;

import com.online.lms.dto.chapter.ChapterDTO;
import com.online.lms.dto.chapter.LessonDTO;
import com.online.lms.dto.course.CourseListItemDTO;
import com.online.lms.entity.Course;
import com.online.lms.enums.CourseStatus;
import com.online.lms.service.CategoryService;
import com.online.lms.service.CourseContentService;
import com.online.lms.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CourseViewerController {

    private final CourseService courseService;
    private final CategoryService categoryService;
    private final CourseContentService courseContentService;

    @GetMapping({"/courses", "/course"})
    public String searchCourses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model
    ) {
        log.info("Hits GET /courses or /course - keyword: {}, categoryId: {}, page: {}, size: {}", keyword, categoryId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<CourseListItemDTO> coursePage = courseService.search(keyword, categoryId, CourseStatus.PUBLISHED, null, pageable);

        model.addAttribute("coursePage", coursePage);
        model.addAttribute("categories", categoryService.findAllActive());
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("currentPage", "courses");

        return "courses";
    }

    @GetMapping("/courses/{id}")
    public String viewCourseDetail(@PathVariable Long id, Model model) {
        log.info("Hits GET /courses/{}", id);
        Course course = courseService.findById(id);
        List<ChapterDTO> chapters = courseContentService.findActiveChaptersByCourse(id);

        model.addAttribute("course", course);
        model.addAttribute("chapters", chapters);
        model.addAttribute("currentPage", "courses");

        return "course-detail";
    }

    @GetMapping("/courses/{courseId}/lessons/{lessonId}")
    @PreAuthorize("hasAnyRole('MEMBER', 'MANAGER', 'ADMIN')")
    public String viewLesson(@PathVariable Long courseId, @PathVariable Long lessonId, Model model) {
        log.info("Hits GET /courses/{}/lessons/{}", courseId, lessonId);
        Course course = courseService.findById(courseId);
        List<ChapterDTO> chapters = courseContentService.findActiveChaptersByCourse(courseId);
        LessonDTO currentLesson = courseContentService.findActiveLessonByCourseAndId(courseId, lessonId);

        model.addAttribute("course", course);
        model.addAttribute("chapters", chapters);
        model.addAttribute("currentLesson", currentLesson);
        model.addAttribute("currentPage", "courses");

        return "lesson-view";
    }
}
