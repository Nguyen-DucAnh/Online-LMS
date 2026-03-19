package com.online.lms.controller;

import com.online.lms.dto.chapter.LessonDTO;
import com.online.lms.exceptions.ResourceNotFoundException;
import com.online.lms.service.CourseContentService;
import com.online.lms.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LessonViewerController {

    private final EnrollmentService enrollmentService;
    private final CourseContentService courseContentService;

    @GetMapping("/lesson-viewer/{enrollmentId}")
    @PreAuthorize("hasAnyRole('MEMBER', 'MANAGER', 'ADMIN')")
    public String openLessonViewer(@PathVariable Long enrollmentId,
                                   RedirectAttributes redirectAttributes) {
        Long courseId;
        try {
            courseId = enrollmentService.getApprovedCourseIdByEnrollmentId(enrollmentId);
        } catch (ResourceNotFoundException | IllegalStateException e) {
            log.warn("Enrollment {} not valid: {}", enrollmentId, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Đăng ký khóa học không hợp lệ hoặc chưa được duyệt.");
            return "redirect:/my-enrollments";
        }

        LessonDTO firstLesson;
        try {
            firstLesson = courseContentService.findFirstActiveLessonByCourse(courseId);
        } catch (ResourceNotFoundException e) {
            log.warn("No active lesson for courseId={}: {}", courseId, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Khóa học chưa có bài học nào để học.");
            return "redirect:/courses/" + courseId;
        }

        return "redirect:/courses/" + courseId + "/lessons/" + firstLesson.getId();
    }
}
