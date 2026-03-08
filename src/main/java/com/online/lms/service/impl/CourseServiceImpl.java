package com.online.lms.service.impl;

import com.online.lms.dto.course.CourseFormDTO;
import com.online.lms.dto.course.CourseListItemDTO;
import com.online.lms.entity.Category;
import com.online.lms.entity.Course;
import com.online.lms.entity.User;
import com.online.lms.entity.enums.CourseStatus;
import com.online.lms.exceptions.ResourceNotFoundException;
import com.online.lms.repository.CategoryRepository;
import com.online.lms.repository.CourseRepository;
import com.online.lms.repository.UserRepository;
import com.online.lms.service.CourseService;
import com.online.lms.utils.CourseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Scope("singleton")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    public Page<CourseListItemDTO> search(String keyword, Integer categoryId,
                                          CourseStatus status, Pageable pageable) {
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return courseRepository.search(kw, categoryId, status, pageable)
                .map(CourseMapper::toListItemDTO);
    }

    @Override
    public CourseFormDTO findFormById(int id) {
        return CourseMapper.toFormDTO(getCourseOrThrow(id));
    }

    @Override
    public Course findById(int id) {
        return getCourseOrThrow(id);
    }

    @Override
    @Transactional
    public void save(CourseFormDTO dto) {
        Course course = Course.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .thumbnail(dto.getThumbnail())
                .listedPrice(dto.getListedPrice())
                .salePrice(dto.getSalePrice())
                .duration(dto.getDuration())
                .status(dto.getStatus() != null ? dto.getStatus() : CourseStatus.UNPUBLISHED)
                .level(dto.getLevel())
                .featured(Boolean.TRUE.equals(dto.getFeatured()))
                .category(getCategoryOrThrow(dto.getCategoryId()))
                .instructor(dto.getInstructorId() != null ? getUserOrThrow(dto.getInstructorId()) : null)
                .build();
        courseRepository.save(course);
        log.info("Course created: title={}", course.getTitle());
    }

    @Override
    @Transactional
    public void update(int id, CourseFormDTO dto) {
        Course course = getCourseOrThrow(id);
        course.setTitle(dto.getTitle());
        course.setDescription(dto.getDescription());
        if (dto.getThumbnail() != null && !dto.getThumbnail().isBlank()) {
            course.setThumbnail(dto.getThumbnail());
        }
        course.setListedPrice(dto.getListedPrice());
        course.setSalePrice(dto.getSalePrice());
        course.setDuration(dto.getDuration());
        course.setLevel(dto.getLevel());
        course.setFeatured(Boolean.TRUE.equals(dto.getFeatured()));
        course.setCategory(getCategoryOrThrow(dto.getCategoryId()));
        course.setInstructor(dto.getInstructorId() != null ? getUserOrThrow(dto.getInstructorId()) : null);
        if (dto.getStatus() != null) course.setStatus(dto.getStatus());
        courseRepository.save(course);
        log.info("Course updated: id={}", id);
    }

    @Override
    @Transactional
    public void toggleStatus(int id) {
        Course course = getCourseOrThrow(id);
        CourseStatus next = course.getStatus() == CourseStatus.PUBLISHED
                ? CourseStatus.UNPUBLISHED : CourseStatus.PUBLISHED;
        course.setStatus(next);
        courseRepository.save(course);
        log.info("Course {} status toggled → {}", id, next);
    }

    @Override
    @Transactional
    public void deleteById(int id) {
        courseRepository.delete(getCourseOrThrow(id));
        log.info("Course deleted: id={}", id);
    }

    // ===== Private helpers =====

    private Course getCourseOrThrow(int id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học id=" + id));
    }

    private Category getCategoryOrThrow(int id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục id=" + id));
    }

    private User getUserOrThrow(int id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng id=" + id));
    }
}
