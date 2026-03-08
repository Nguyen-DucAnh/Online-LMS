package com.online.lms.service;

import com.online.lms.dto.course.CourseFormDTO;
import com.online.lms.dto.course.CourseListItemDTO;
import com.online.lms.entity.Course;
import com.online.lms.entity.enums.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CourseService {

    Page<CourseListItemDTO> search(String keyword, Integer categoryId, CourseStatus status, Pageable pageable);

    CourseFormDTO findFormById(int id);

    Course findById(int id);

    void save(CourseFormDTO dto);

    void update(int id, CourseFormDTO dto);

    void toggleStatus(int id);

    void deleteById(int id);
}
