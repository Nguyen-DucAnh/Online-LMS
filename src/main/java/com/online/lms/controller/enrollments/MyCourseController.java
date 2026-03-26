package com.online.lms.controller.enrollments;

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

    @GetMapping("/my-courses")
    @PreAuthorize("hasAnyRole('MEMBER', 'MANAGER', 'ADMIN')")
    public String myCourses(Model model) {
        log.info("Hits GET /my-courses");
        model.addAttribute("courses", enrollmentService.getMyCourses());
        model.addAttribute("currentPage", "my-courses");
        return "my-courses";
    }
}