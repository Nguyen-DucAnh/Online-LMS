package com.online.lms.service.impl;

import com.online.lms.dto.enrollment.MyCourseDTO;
import com.online.lms.dto.enrollment.MyEnrollmentDTO;
import com.online.lms.entity.Course;
import com.online.lms.entity.Enrollment;
import com.online.lms.entity.User;
import com.online.lms.enums.EnrollmentStatus;
import com.online.lms.exceptions.ResourceNotFoundException;
import com.online.lms.repository.EnrollmentRepository;
import com.online.lms.repository.UserRepository;
import com.online.lms.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository       userRepository;

    // ─── My Enrollments ───────────────────────────────────────────────────────

    @Override
    public List<MyEnrollmentDTO> getMyEnrollments() {
        User currentUser = getCurrentUser();
        log.info("getMyEnrollments - userId={}", currentUser.getId());

        return enrollmentRepository
                .findAllByUserIdOrderByEnrollDateDesc(currentUser.getId())
                .stream()
                .map(this::toMyEnrollmentDTO)
                .toList();
    }

    // ─── My Courses ───────────────────────────────────────────────────────────

    @Override
    public List<MyCourseDTO> getMyCourses() {
        User currentUser = getCurrentUser();
        log.info("getMyCourses - userId={}", currentUser.getId());
        return enrollmentRepository
                .findApprovedByUserId(currentUser.getId())
                .stream()
                .map(this::toMyCourseDTO)
                .toList();
    }

    // ─── Access control ───────────────────────────────────────────────────────

    @Override
    public boolean hasAccessToCourse(Long courseId) {
        User currentUser = getCurrentUser();
        return enrollmentRepository.existsByUser_IdAndCourse_IdAndStatus(
                currentUser.getId(), courseId, EnrollmentStatus.APPROVED);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Lấy user hiện tại — theo đúng pattern team:
     * SecurityContextHolder → email → query DB.
     */
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy người dùng: " + email));
    }

    /**
     * Map Enrollment entity → MyEnrollmentDTO.
     * Tất cả relation đã được JOIN FETCH trong repository → không có lazy load ở đây.
     */
    private MyEnrollmentDTO toMyEnrollmentDTO(Enrollment e) {
        Course c = e.getCourse();
        return MyEnrollmentDTO.builder()
                .id(e.getId())
                .courseId(c.getId())
                .courseTitle(c.getTitle())
                .courseThumbnail(c.getThumbnail())
                .categoryName(c.getCategory() != null ? c.getCategory().getName() : null)
                .fullName(e.getFullName())
                .email(e.getEmail())
                .enrollDate(e.getEnrollDate())
                .fee(e.getFee())
                .status(e.getStatus())
                .rejectedNotes(e.getRejectedNotes())
                .progress(e.getProgress())
                .completedAt(e.getCompletedAt())
                .build();
    }

    /**
     * Map Enrollment → MyCourseDTO.
     *
     * progressInt: cần là int để CSS width không bị "45.50%".
     * completed  : true khi completedAt != null (hoặc progress == 100).
     */
    private MyCourseDTO toMyCourseDTO(Enrollment e) {
        Course c = e.getCourse();

        BigDecimal progress = e.getProgress() != null ? e.getProgress() : BigDecimal.ZERO;
        int progressInt = progress.intValue(); // VD: 45.50 → 45
        boolean completed = e.getCompletedAt() != null
                || progress.compareTo(new BigDecimal("100")) >= 0;

        return MyCourseDTO.builder()
                .enrollmentId(e.getId())
                .courseId(c.getId())
                .courseTitle(c.getTitle())
                .courseThumbnail(c.getThumbnail())
                .categoryName(c.getCategory() != null ? c.getCategory().getName() : null)
                .instructorName(c.getInstructor() != null ? c.getInstructor().getFullName() : null)
                .enrollDate(e.getEnrollDate())
                .progress(progress)
                .progressInt(progressInt)
                .completed(completed)
                .build();
    }
}