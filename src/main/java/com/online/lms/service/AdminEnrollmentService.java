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

    // ── List ──────────────────────────────────────────────────────────────────

    /** Admin: xem tất cả enrollments */
    Page<EnrollmentListItemDTO> findAll(Long courseId, Long userId,
                                        EnrollmentStatus status, String keyword,
                                        Pageable pageable);

    /** Manager: chỉ xem enrollments của courses mình phụ trách */
    Page<EnrollmentListItemDTO> findByInstructor(Long instructorId,
                                                 Long courseId,
                                                 EnrollmentStatus status,
                                                 String keyword,
                                                 Pageable pageable);

    // ── Form ──────────────────────────────────────────────────────────────────

    /** Load form để edit enrollment */
    EnrollmentFormDTO findFormById(Long id);

    /** Admin tạo mới hoặc cập nhật enrollment */
    void save(EnrollmentFormDTO dto);

    // ── Actions ───────────────────────────────────────────────────────────────

    /** Approve enrollment */
    void approve(Long id);

    /** Reject enrollment với lý do */
    void reject(Long id, String rejectedNotes);

    /** Xóa enrollment (chỉ enrollment do chính mình tạo) */
    void delete(Long id);

    /**
     * Kiểm tra manager có quyền truy cập enrollment này không.
     * Dùng trong controller để check trước khi edit/delete.
     */
    boolean hasManagerAccess(Long enrollmentId, Long managerId);

    // ── Import/Export ─────────────────────────────────────────────────────────

    /** Export danh sách enrollment ra file CSV */
    byte[] exportToCsv(Long courseId, EnrollmentStatus status) throws IOException;

    /** Manager export enrollments của courses được giao */
    byte[] exportToCsvByInstructor(Long instructorId, Long courseId, EnrollmentStatus status) throws IOException;

    /** Import danh sách enrollment từ Excel */
    int importFromExcel(Long courseId, MultipartFile file) throws IOException;

    /** Danh sách user cho dropdown filter ở Enrollment List */
    List<User> findFilterUsers();

    /** Danh sách user cho dropdown filter của manager */
    List<User> findFilterUsersByInstructor(Long instructorId);
}