package com.online.lms.controller;


import com.online.lms.dto.course.CourseListItemDTO;
import com.online.lms.enums.CourseStatus;
import com.online.lms.enums.UserRole;
import com.online.lms.repository.CourseRepository;
import com.online.lms.repository.UserRepository;
import com.online.lms.service.CategoryService;
import com.online.lms.service.CourseContentService;
import com.online.lms.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final CourseService courseService;
    private final CategoryService categoryService;
    private final CourseContentService courseContentService;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    @GetMapping("/")
    public String home(Model model) {
        // Fetch top 6 latest published courses
        Page<CourseListItemDTO> latestCourses = courseService.search(
                null, null, CourseStatus.PUBLISHED,
                PageRequest.of(0, 6, Sort.by(Sort.Direction.DESC, "id"))
        );
        model.addAttribute("latestCourses", latestCourses.getContent());
        return "home";
    }

    @GetMapping("/courses")
    public String courses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        Page<CourseListItemDTO> coursePage = courseService.search(
                keyword, categoryId, CourseStatus.PUBLISHED,
                PageRequest.of(page, 9, Sort.by(Sort.Direction.DESC, "id"))
        );
        model.addAttribute("coursePage", coursePage);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        return "courses";
    }

    @GetMapping("/courses/{id}")
    public String courseDetail(@PathVariable Long id, Model model) {
        model.addAttribute("course", courseService.findById(id));
        model.addAttribute("chapters", courseContentService.findChaptersByCourse(id));
        return "course-detail";
    }

    @GetMapping("/courses/{id}/lessons/{lessonId}")
    public String lessonView(
            @PathVariable Long id,
            @PathVariable Long lessonId,
            Model model
    ) {
        model.addAttribute("course", courseService.findById(id));
        model.addAttribute("chapters", courseContentService.findChaptersByCourse(id));
        model.addAttribute("currentLesson", courseContentService.findLessonById(lessonId));
        return "lesson-view";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("totalStudents", userRepository.countByRole(UserRole.MEMBER));
        model.addAttribute("totalCourses", courseRepository.count());
        model.addAttribute("totalInstructors", userRepository.countByRole(UserRole.MANAGER));
        
        // Fetch 5 latest courses for the activity table
        Page<CourseListItemDTO> recentCourses = courseService.search(
                null, null, null, 
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "id"))
        );
        model.addAttribute("recentCourses", recentCourses.getContent());
        
        return "admin/dashboard";
    }

    @GetMapping("/manager/dashboard")
    public String managerDashboard(Model model) {
        // Manager dashboard can also show some stats if needed
        return "manager/dashboard";
    }
}
