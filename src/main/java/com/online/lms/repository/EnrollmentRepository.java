package com.online.lms.repository;

import com.online.lms.entity.Enrollment;
import com.online.lms.enums.EnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    // ─── My Enrollments ───────────────────────────────────────────────────────

    /**
     * Lấy tất cả enrollment của 1 user, mới nhất trước.
     * JOIN FETCH course + category trong 1 query → tránh N+1 khi render table.
     */
    @Query("""
        SELECT e FROM Enrollment e
        JOIN FETCH e.course c
        LEFT JOIN FETCH c.category
        WHERE e.user.id = :userId
        ORDER BY e.enrollDate DESC
        """)
    List<Enrollment> findAllByUserIdOrderByEnrollDateDesc(@Param("userId") Long userId);

    // ─── My Courses (chỉ APPROVED) ────────────────────────────────────────────

    @Query("""
        SELECT e FROM Enrollment e
        JOIN FETCH e.course c
        LEFT JOIN FETCH c.category
        LEFT JOIN FETCH c.instructor
        WHERE e.user.id = :userId
          AND e.status = com.online.lms.enums.EnrollmentStatus.APPROVED
        ORDER BY e.enrollDate DESC
        """)
    List<Enrollment> findApprovedByUserId(@Param("userId") Long userId);

    // ─── Access control (Lesson Viewer dùng) ──────────────────────────────────

    /**
     * Kiểm tra user có enrollment APPROVED cho course không.
     * Dùng trong Lesson Viewer để block truy cập trái phép.
     */
    boolean existsByUser_IdAndCourse_IdAndStatus(
            Long userId, Long courseId, EnrollmentStatus status);

    /**
     * Kiểm tra user đã có enrollment nào cho course chưa (bất kỳ status).
     * Dùng trong Learning Enroll để tránh đăng ký trùng.
     */
    Optional<Enrollment> findByUser_IdAndCourse_Id(Long userId, Long courseId);

    // ─── Enrollment List (Admin/Manager) ──────────────────────────────────────

    /**
     * Admin: xem tất cả, có filter + search + pagination.
     * countQuery riêng để Spring không COUNT với JOIN FETCH (lỗi thường gặp).
     */
    @Query(value = """
        SELECT e FROM Enrollment e
        JOIN FETCH e.course c
        JOIN FETCH e.user u
        LEFT JOIN FETCH c.category
        WHERE (:status IS NULL OR e.status = :status)
          AND (:keyword IS NULL
               OR LOWER(e.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(e.email)    LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(c.title)    LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY e.enrollDate DESC
        """,
            countQuery = """
        SELECT COUNT(e) FROM Enrollment e
        JOIN e.course c
        WHERE (:status IS NULL OR e.status = :status)
          AND (:keyword IS NULL
               OR LOWER(e.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(e.email)    LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(c.title)    LIKE LOWER(CONCAT('%', :keyword, '%')))
        """)
    Page<Enrollment> findAllWithFilter(
            @Param("status")  EnrollmentStatus status,
            @Param("keyword") String keyword,
            Pageable pageable);

    /**
     * Manager: chỉ thấy enrollment của course mình là instructor.
     */
    @Query(value = """
        SELECT e FROM Enrollment e
        JOIN FETCH e.course c
        JOIN FETCH e.user u
        LEFT JOIN FETCH c.category
        WHERE c.instructor.id = :instructorId
          AND (:status IS NULL OR e.status = :status)
          AND (:keyword IS NULL
               OR LOWER(e.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(e.email)    LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(c.title)    LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY e.enrollDate DESC
        """,
            countQuery = """
        SELECT COUNT(e) FROM Enrollment e
        JOIN e.course c
        WHERE c.instructor.id = :instructorId
          AND (:status IS NULL OR e.status = :status)
          AND (:keyword IS NULL
               OR LOWER(e.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(e.email)    LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(c.title)    LIKE LOWER(CONCAT('%', :keyword, '%')))
        """)
    Page<Enrollment> findByInstructorWithFilter(
            @Param("instructorId") Long instructorId,
            @Param("status")       EnrollmentStatus status,
            @Param("keyword")      String keyword,
            Pageable pageable);

    // ─── Enrollment Details ────────────────────────────────────────────────────

    /**
     * Load 1 enrollment kèm đầy đủ relations trong 1 query.
     * Tránh LazyInitializationException trong controller/template.
     */
    @Query("""
        SELECT e FROM Enrollment e
        JOIN FETCH e.course c
        JOIN FETCH e.user u
        LEFT JOIN FETCH c.category
        LEFT JOIN FETCH c.instructor
        WHERE e.id = :id
        """)
    Optional<Enrollment> findByIdWithDetails(@Param("id") Long id);

    /**
     * Manager chỉ được xem enrollment thuộc course của mình.
     * Nếu empty → controller trả 403.
     */
    @Query("""
        SELECT e FROM Enrollment e
        JOIN FETCH e.course c
        JOIN FETCH e.user u
        WHERE e.id = :enrollmentId
          AND c.instructor.id = :instructorId
        """)
    Optional<Enrollment> findByIdAndInstructorId(
            @Param("enrollmentId")  Long enrollmentId,
            @Param("instructorId")  Long instructorId);
}