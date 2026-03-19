package com.online.lms.service.impl;

import com.online.lms.dto.course.CourseFormDTO;
import com.online.lms.dto.course.CourseListItemDTO;
import com.online.lms.entity.Category;
import com.online.lms.entity.Course;
import com.online.lms.entity.User;
import com.online.lms.enums.CourseLevel;
import com.online.lms.enums.CourseStatus;
import com.online.lms.enums.UserRole;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public Page<CourseListItemDTO> search(String keyword, Long categoryId,
                                          CourseStatus status, Long instructorId, Pageable pageable) {
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return courseRepository.search(kw, categoryId, status, instructorId, pageable)
                .map(CourseMapper::toListItemDTO);
    }

    @Override
    public Page<CourseListItemDTO> searchPublished(String keyword, Long categoryId,
                                                    CourseLevel level, Pageable pageable) {
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return null;
    }

    @Override
    public long countPublished() {
        return courseRepository.countByStatus(CourseStatus.PUBLISHED);
    }

    @Override
    public CourseFormDTO findFormById(Long id) {
        return CourseMapper.toFormDTO(getCourseOrThrow(id));
    }

    @Override
    public Course findById(Long id) {
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
    public void update(Long id, CourseFormDTO dto) {
        Course course = getCourseOrThrow(id);
        User currentUser = getCurrentUser();

        course.setTitle(dto.getTitle());
        course.setDescription(dto.getDescription());
        if (dto.getThumbnail() != null && !dto.getThumbnail().isBlank()) {
            course.setThumbnail(dto.getThumbnail());
        }

        // Only Admin can change price and status
        if (currentUser.getRole() == UserRole.ADMIN) {
            course.setListedPrice(dto.getListedPrice());
            course.setSalePrice(dto.getSalePrice());
            if (dto.getStatus() != null) course.setStatus(dto.getStatus());
        } else {
            log.warn("Manager {} attempted to change price/status for course {}. Ignoring.", currentUser.getEmail(), id);
        }

        course.setDuration(dto.getDuration());
        course.setLevel(dto.getLevel());
        course.setFeatured(Boolean.TRUE.equals(dto.getFeatured()));
        course.setCategory(getCategoryOrThrow(dto.getCategoryId()));

        // Only Admin can change instructor
        if (currentUser.getRole() == UserRole.ADMIN) {
            course.setInstructor(dto.getInstructorId() != null ? getUserOrThrow(dto.getInstructorId()) : null);
        }

        courseRepository.save(course);
        log.info("Course updated: id={}", id);
    }

    @Override
    @Transactional
    public void toggleStatus(Long id) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Only Admin can toggle course status");
        }
        Course course = getCourseOrThrow(id);
        CourseStatus next = course.getStatus() == CourseStatus.PUBLISHED
                ? CourseStatus.UNPUBLISHED : CourseStatus.PUBLISHED;
        course.setStatus(next);
        courseRepository.save(course);
        log.info("Course {} status toggled → {}", id, next);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Course course = getCourseOrThrow(id);
        // Requirement: Only new course (not related to any system transactions) can be deleted
        // Based on entities, checking if course is linked to any student (user_id field)
        if (course.getUser() != null) {
            throw new RuntimeException("Cannot delete course that has been enrolled by students!");
        }
        courseRepository.delete(course);
        log.info("Course deleted: id={}", id);
    }

    // ===== Private helpers =====

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Course getCourseOrThrow(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học id=" + id));
    }

    private Category getCategoryOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục id=" + id));
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng id=" + id));
    }

    @Override
    public List<Course> findAll() {
        return courseRepository.findAll();
    }
}