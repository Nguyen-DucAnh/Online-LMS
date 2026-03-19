package com.online.lms.controller;

import com.online.lms.dto.chapter.LessonDTO;
import com.online.lms.service.CourseContentService;
import com.online.lms.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class LessonViewerController {

    private final EnrollmentService enrollmentService;
    private final CourseContentService courseContentService;

    @GetMapping("/lesson-viewer/{enrollmentId}")
    @PreAuthorize("hasAnyRole('MEMBER', 'MANAGER', 'ADMIN')")
    public String openLessonViewer(@PathVariable Long enrollmentId) {
        Long courseId = enrollmentService.getApprovedCourseIdByEnrollmentId(enrollmentId);
        LessonDTO firstLesson = courseContentService.findFirstActiveLessonByCourse(courseId);
        return "redirect:/courses/" + courseId + "/lessons/" + firstLesson.getId();
    }
}
