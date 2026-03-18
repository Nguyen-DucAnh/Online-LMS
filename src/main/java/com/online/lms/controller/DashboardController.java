package com.online.lms.controller.admin;

import com.online.lms.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping
    public String showDashboard(Model model) {


        model.addAttribute("totalUsers", dashboardService.getTotalUsers());
        model.addAttribute("totalCourses", dashboardService.getTotalCourses());
        model.addAttribute("activeSettings", dashboardService.getActiveSettings());
        model.addAttribute("publishedCourses", dashboardService.getPublishedCourses());


        model.addAttribute("recentCourses", dashboardService.getRecentCourses());
        model.addAttribute("recentUsers", dashboardService.getRecentUsers());

        return "admin/dashboard";
    }
}