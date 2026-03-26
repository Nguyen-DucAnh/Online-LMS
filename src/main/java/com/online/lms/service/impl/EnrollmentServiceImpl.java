package com.online.lms.service.impl;

import com.online.lms.dto.enrollment.EnrollmentRequestDTO;
import com.online.lms.dto.enrollment.MyCourseDTO;
import com.online.lms.dto.enrollment.MyEnrollmentDTO;
import com.online.lms.entity.Course;
import com.online.lms.entity.Enrollment;
import com.online.lms.entity.User;
import com.online.lms.enums.EnrollmentStatus;
import com.online.lms.exceptions.ResourceNotFoundException;
import com.online.lms.repository.EnrollmentRepository;
import com.online.lms.repository.UserRepository;
import com.online.lms.service.CourseService;
import com.online.lms.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository       userRepository;
    private final CourseService        courseService;

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

    @Override
    public boolean hasAccessToCourse(Long courseId) {
        User currentUser = getCurrentUser();
        return enrollmentRepository.existsByUser_IdAndCourse_IdAndStatus(
                currentUser.getId(), courseId, EnrollmentStatus.APPROVED);
    }

    @Override
    public Long getApprovedCourseIdByEnrollmentId(Long enrollmentId) {
        User currentUser = getCurrentUser();
        Enrollment enrollment = enrollmentRepository
                .findApprovedByIdAndUserId(enrollmentId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy enrollment đã duyệt id=" + enrollmentId));
        return enrollment.getCourse().getId();
    }

    @Override
    @Transactional
    public Long enroll(Long courseId, EnrollmentRequestDTO dto) {
        User currentUser = getCurrentUser();
        log.info("enroll - userId={}, courseId={}", currentUser.getId(), courseId);

        // Check duplicate PENDING or APPROVED enrollment
        Optional<Enrollment> existing = enrollmentRepository
                .findByUser_IdAndCourse_Id(currentUser.getId(), courseId);
        if (existing.isPresent()) {
            EnrollmentStatus s = existing.get().getStatus();
            if (s == EnrollmentStatus.PENDING) {
                throw new IllegalStateException("Bạn đã đăng ký khóa học này và đang chờ duyệt.");
            }
            if (s == EnrollmentStatus.APPROVED) {
                throw new IllegalStateException("Bạn đã được duyệt vào khóa học này.");
            }
        }

        Course course = courseService.findById(courseId);

        Enrollment enrollment = Enrollment.builder()
                .user(currentUser)
                .course(course)
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .enrollNote(dto.getEnrollNote())
                .paymentMethod(dto.getPaymentMethod())
                .fee(course.getSalePrice() != null ? course.getSalePrice() : BigDecimal.ZERO)
                .status(EnrollmentStatus.PENDING)
                .build();

        enrollmentRepository.save(enrollment);
        log.info("Enrollment created id={}", enrollment.getId());
        return enrollment.getId();
    }

    @Override
    public Optional<EnrollmentStatus> getExistingEnrollmentStatus(Long courseId) {
        User currentUser = getCurrentUser();
        return enrollmentRepository
                .findByUser_IdAndCourse_Id(currentUser.getId(), courseId)
                .map(Enrollment::getStatus);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy người dùng: " + email));
    }

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

    private MyCourseDTO toMyCourseDTO(Enrollment e) {
        Course c = e.getCourse();

        BigDecimal progress = e.getProgress() != null ? e.getProgress() : BigDecimal.ZERO;
        int progressInt = progress.intValue();
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