package com.online.lms.service;

import com.online.lms.dto.enrollment.MyCourseDTO;
import com.online.lms.dto.enrollment.MyEnrollmentDTO;

import java.util.List;

public interface EnrollmentService {

    /**
     * Lấy danh sách tất cả enrollment của user hiện tại.
     * Dùng cho trang My Enrollments.
     */
    List<MyEnrollmentDTO> getMyEnrollments();

    /**
     * Lấy danh sách khóa học đã được APPROVED của user hiện tại.
     * Dùng cho trang My Courses.
     */
    List<MyCourseDTO> getMyCourses();

    /**
     * Kiểm tra user hiện tại có enrollment APPROVED cho course không.
     * Dùng cho Lesson Viewer access control.
     */
    boolean hasAccessToCourse(Long courseId);

    Long getApprovedCourseIdByEnrollmentId(Long enrollmentId);
}
