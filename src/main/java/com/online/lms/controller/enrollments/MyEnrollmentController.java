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
public class MyEnrollmentController {

    private final EnrollmentService enrollmentService;

    @GetMapping("/my-enrollments")
    @PreAuthorize("hasAnyRole('MEMBER', 'MANAGER', 'ADMIN')")
    public String myEnrollments(Model model) {
        log.info("Hits GET /my-enrollments");
        model.addAttribute("enrollments", enrollmentService.getMyEnrollments());
        model.addAttribute("currentPage", "my-enrollments");
        return "enrollment/my-enrollments";
    }
}
