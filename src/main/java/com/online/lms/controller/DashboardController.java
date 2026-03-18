package com.online.lms.controller.admin;

import com.online.lms.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import com.online.lms.dto.course.CourseListItemDTO;
import com.online.lms.entity.Course;
import com.online.lms.enums.CourseStatus;
import com.online.lms.enums.UserRole;
import com.online.lms.exceptions.ResourceNotFoundException;
import com.online.lms.repository.CourseRepository;
import com.online.lms.repository.UserRepository;
import com.online.lms.service.CategoryService;
import com.online.lms.service.CourseContentService;
import com.online.lms.service.CourseService;
import com.online.lms.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/dashboard")
public class DashboardController {


    @Autowired
    private DashboardService dashboardService;

    private final CourseService courseService;
    private final CategoryService categoryService;
    private final CourseContentService courseContentService;
    private final EnrollmentService enrollmentService;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;


    @GetMapping
    public String showDashboard(Model model) {


        model.addAttribute("totalUsers", dashboardService.getTotalUsers());
        model.addAttribute("totalCourses", dashboardService.getTotalCourses());
        model.addAttribute("activeSettings", dashboardService.getActiveSettings());
        model.addAttribute("publishedCourses", dashboardService.getPublishedCourses());


    @GetMapping("/courses/{id}/lessons/{lessonId}")
    public String lessonView(
            @PathVariable Long id,
            @PathVariable Long lessonId,
            Model model
    ) {
        Course course = courseService.findById(id);
        if (!enrollmentService.hasAccessToCourse(id)) {
            throw new ResourceNotFoundException("Bạn chưa có quyền truy cập khóa học này");
        }

        model.addAttribute("course", course);
        model.addAttribute("chapters", courseContentService.findActiveChaptersByCourse(id));
        model.addAttribute("currentLesson", courseContentService.findActiveLessonByCourseAndId(id, lessonId));
        return "lesson-view";
    }

        model.addAttribute("recentCourses", dashboardService.getRecentCourses());
        model.addAttribute("recentUsers", dashboardService.getRecentUsers());

        return "admin/dashboard";
    }
}