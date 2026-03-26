package com.online.lms.service;

import com.online.lms.dto.enrollment.EnrollmentRequestDTO;
import com.online.lms.dto.enrollment.MyCourseDTO;
import com.online.lms.dto.enrollment.MyEnrollmentDTO;
import com.online.lms.enums.EnrollmentStatus;

import java.util.List;
import java.util.Optional;

public interface EnrollmentService {


    List<MyEnrollmentDTO> getMyEnrollments();

    List<MyCourseDTO> getMyCourses();

    boolean hasAccessToCourse(Long courseId);

    Long getApprovedCourseIdByEnrollmentId(Long enrollmentId);

    Long enroll(Long courseId, EnrollmentRequestDTO dto);

    Optional<EnrollmentStatus> getExistingEnrollmentStatus(Long courseId);
}
