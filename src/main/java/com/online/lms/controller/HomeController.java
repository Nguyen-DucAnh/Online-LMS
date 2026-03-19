package com.online.lms.controller;

import com.online.lms.repository.CourseRepository;
import com.online.lms.utils.CourseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final CourseRepository courseRepository;

    @GetMapping("/")
    public String home(Model model) {
        var latestCourses = courseRepository.findTop5ByOrderByIdDesc()
                .stream()
                .map(CourseMapper::toListItemDTO)
                .toList();
        model.addAttribute("latestCourses", latestCourses);
        return "home";
    }
}
