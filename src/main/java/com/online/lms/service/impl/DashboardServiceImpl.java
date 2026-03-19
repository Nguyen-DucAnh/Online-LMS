package com.online.lms.service.impl;

import com.online.lms.entity.Course;
import com.online.lms.entity.User;
import com.online.lms.repository.CourseRepository;
import com.online.lms.repository.SettingRepository;
import com.online.lms.repository.UserRepository;
import com.online.lms.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SettingRepository settingRepository;

    @Override
    public long getTotalUsers() {
        return userRepository.count();
    }

    @Override
    public long getTotalCourses() {
        return courseRepository.count();
    }

    @Override
    public long getPublishedCourses() {

        return courseRepository.count();
    }

    @Override
    public long getActiveSettings() {

        return settingRepository.findAll().stream()
                .filter(s -> "Active".equalsIgnoreCase(s.getStatus())).count();
    }

    @Override
    public List<Course> getRecentCourses() {
        return courseRepository.findTop5ByOrderByIdDesc();
    }

    @Override
    public List<User> getRecentUsers() {
        return userRepository.findTop5ByOrderByIdDesc();
    }
}