package com.online.lms.controller;

import com.online.lms.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MyCourseController {

    private final EnrollmentService enrollmentService;

    /**
     * GET /my-courses
     *
     * Chỉ MEMBER và MANAGER truy cập.
     * ADMIN dùng /admin/enrollments để quản lý.
     *
     * Template: templates/my-courses.html
     * Model attribute: "courses" → List<MyCourseDTO>
     *
     * Lưu ý: template dùng biến "courses" (không phải "enrollments")
     * khớp với my-courses.html của team: th:each="course : ${courses}"
     */
    @GetMapping("/my-courses")
    @PreAuthorize("hasAnyRole('MEMBER', 'MANAGER', 'ADMIN')")
    public String myCourses(Model model) {
        log.info("Hits GET /my-courses");
        model.addAttribute("courses", enrollmentService.getMyCourses());
        model.addAttribute("currentPage", "my-courses");
        return "my-courses";
    }
}