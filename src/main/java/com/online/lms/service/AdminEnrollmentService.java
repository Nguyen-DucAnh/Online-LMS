package com.online.lms.service;

import com.online.lms.dto.enrollment.EnrollmentFormDTO;
import com.online.lms.dto.enrollment.EnrollmentListItemDTO;
import com.online.lms.entity.User;
import com.online.lms.enums.EnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface AdminEnrollmentService {

    Page<EnrollmentListItemDTO> findAll(Long courseId, Long userId,
                                        EnrollmentStatus status, String keyword,
                                        Pageable pageable);

    Page<EnrollmentListItemDTO> findByInstructor(Long instructorId,
                                                 Long courseId,
                                                 EnrollmentStatus status,
                                                 String keyword,
                                                 Pageable pageable);


    EnrollmentFormDTO findFormById(Long id);

    void save(EnrollmentFormDTO dto);


    void approve(Long id);

    void reject(Long id, String rejectedNotes);

    void delete(Long id);

    boolean hasManagerAccess(Long enrollmentId, Long managerId);

    byte[] exportToCsv(Long courseId, EnrollmentStatus status) throws IOException;

    byte[] exportToCsvByInstructor(Long instructorId, Long courseId, EnrollmentStatus status) throws IOException;

    int importFromExcel(Long courseId, MultipartFile file) throws IOException;

    List<User> findFilterUsers();

    List<User> findFilterUsersByInstructor(Long instructorId);
}