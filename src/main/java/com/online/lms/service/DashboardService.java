package com.online.lms.service;

import com.online.lms.entity.Course;
import com.online.lms.entity.User;

import java.util.List;

public interface DashboardService {

    long getTotalUsers();

    long getTotalCourses();

    long getPublishedCourses();

    long getActiveSettings();

    List<Course> getRecentCourses();

    List<User> getRecentUsers();
}