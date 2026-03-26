package com.online.lms.controller;

import com.online.lms.dto.chapter.ChapterDTO;
import com.online.lms.dto.chapter.LessonDTO;
import com.online.lms.dto.course.CourseListItemDTO;
import com.online.lms.entity.Course;
import com.online.lms.enums.CourseStatus;
import com.online.lms.enums.EnrollmentStatus;
import com.online.lms.exceptions.ResourceNotFoundException;
import com.online.lms.service.CategoryService;
import com.online.lms.service.CourseContentService;
import com.online.lms.service.CourseService;
import com.online.lms.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CourseViewerController {

    private final CourseService        courseService;
    private final CategoryService      categoryService;
    private final CourseContentService courseContentService;
    private final EnrollmentService    enrollmentService;

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
    public String viewCourseDetail(@PathVariable Long id, Model model,
                                   RedirectAttributes redirectAttributes) {
        log.info("Hits GET /courses/{}", id);
        Course course;
        try {
            course = courseService.findById(id);
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy khóa học.");
            return "redirect:/courses";
        }

        if (course.getStatus() != CourseStatus.PUBLISHED) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdminOrManager = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                            || a.getAuthority().equals("ROLE_MANAGER"));
            if (!isAdminOrManager) {
                redirectAttributes.addFlashAttribute("error", "Khóa học này chưa được phát hành.");
                return "redirect:/courses";
            }
        }

        List<ChapterDTO> chapters = courseContentService.findActiveChaptersByCourse(id);


        String enrollmentStatus = "NONE";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isLoggedIn = auth != null && auth.isAuthenticated()
                && !auth.getPrincipal().equals("anonymousUser");
        if (isLoggedIn) {
            try {
                Optional<EnrollmentStatus> status = enrollmentService.getExistingEnrollmentStatus(id);
                enrollmentStatus = status.map(EnrollmentStatus::name).orElse("NONE");
            } catch (Exception ignored) {
            }
        }

        model.addAttribute("course", course);
        model.addAttribute("chapters", chapters);
        model.addAttribute("currentPage", "courses");
        model.addAttribute("enrollmentStatus", enrollmentStatus);
        model.addAttribute("isLoggedIn", isLoggedIn);

        return "course-detail";
    }

    @GetMapping("/courses/{courseId}/lessons/{lessonId}")
    @PreAuthorize("hasAnyRole('MEMBER', 'MANAGER', 'ADMIN')")
    public String viewLesson(@PathVariable Long courseId, @PathVariable Long lessonId,
                             Model model, RedirectAttributes ra) {
        log.info("Hits GET /courses/{}/lessons/{}", courseId, lessonId);

        // Access control: check if user has APPROVED enrollment for this course
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdminOrManager = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_MANAGER"));

        if (!isAdminOrManager && !enrollmentService.hasAccessToCourse(courseId)) {
            ra.addFlashAttribute("error",
                    "Bạn cần đăng ký và được duyệt trước khi xem bài học này.");
            return "redirect:/courses/" + courseId;
        }

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
