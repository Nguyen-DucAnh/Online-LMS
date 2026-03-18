package com.online.lms.service;

import com.online.lms.dto.course.CourseFormDTO;
import com.online.lms.dto.course.CourseListItemDTO;
import com.online.lms.entity.Course;
import com.online.lms.enums.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CourseService {

    Page<CourseListItemDTO> search(String keyword, Long categoryId, CourseStatus status, Long instructorId, Pageable pageable);

    CourseFormDTO findFormById(Long id);

    Course findById(Long id);

    void save(CourseFormDTO dto);

    void update(Long id, CourseFormDTO dto);

    void toggleStatus(Long id);

    void deleteById(Long id);

    /**
     * Lấy tất cả courses (dùng cho dropdown trong form).
     * Thêm mới — AdminEnrollmentController cần.
     */
    List<Course> findAll();
}
