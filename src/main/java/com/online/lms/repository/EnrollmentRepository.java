package com.online.lms.repository;

import com.online.lms.entity.Enrollment;
import com.online.lms.entity.User;
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

    @Query("""
        SELECT e FROM Enrollment e
        JOIN FETCH e.course c
        LEFT JOIN FETCH c.category
        WHERE e.user.id = :userId
        ORDER BY e.enrollDate DESC
        """)
    List<Enrollment> findAllByUserIdOrderByEnrollDateDesc(@Param("userId") Long userId);


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

    boolean existsByUser_IdAndCourse_IdAndStatus(Long userId, Long courseId, EnrollmentStatus status);

    Optional<Enrollment> findByUser_IdAndCourse_Id(Long userId, Long courseId);

    @Query("""
        SELECT e FROM Enrollment e
        JOIN FETCH e.course c
        WHERE e.id = :enrollmentId
          AND e.user.id = :userId
          AND e.status = com.online.lms.enums.EnrollmentStatus.APPROVED
        """)
    Optional<Enrollment> findApprovedByIdAndUserId(@Param("enrollmentId") Long enrollmentId,
                                                   @Param("userId") Long userId);

    @Query(value = """
        SELECT e FROM Enrollment e
        JOIN FETCH e.course c
        JOIN FETCH e.user u
        LEFT JOIN FETCH c.category
        WHERE (:courseId IS NULL OR c.id = :courseId)
          AND (:userId   IS NULL OR u.id = :userId)
          AND (:status   IS NULL OR e.status = :status)
          AND (:keyword  IS NULL
               OR LOWER(e.fullName) LIKE LOWER(CONCAT('%',:keyword,'%'))
             OR LOWER(FUNCTION('TRANSLATE', e.fullName,
               'áàảãạăắằẳẵặâấầẩẫậđéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵ',
               'aaaaaaaaaaaaaaaaadeeeeeeeeeeeiiiiiooooooooooooooooouuuuuuuuuuuyyyyy'))
              LIKE CONCAT('%', LOWER(FUNCTION('TRANSLATE', :keyword,
               'áàảãạăắằẳẵặâấầẩẫậđéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵ',
               'aaaaaaaaaaaaaaaaadeeeeeeeeeeeiiiiiooooooooooooooooouuuuuuuuuuuyyyyy')), '%')
               OR LOWER(e.email)    LIKE LOWER(CONCAT('%',:keyword,'%'))
               OR LOWER(c.title)    LIKE LOWER(CONCAT('%',:keyword,'%')))
        """,
            countQuery = """
        SELECT COUNT(e) FROM Enrollment e
        JOIN e.course c JOIN e.user u
        WHERE (:courseId IS NULL OR c.id = :courseId)
          AND (:userId   IS NULL OR u.id = :userId)
          AND (:status   IS NULL OR e.status = :status)
          AND (:keyword  IS NULL
               OR LOWER(e.fullName) LIKE LOWER(CONCAT('%',:keyword,'%'))
               OR LOWER(FUNCTION('TRANSLATE', e.fullName,
                   'áàảãạăắằẳẵặâấầẩẫậđéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵ',
                   'aaaaaaaaaaaaaaaaadeeeeeeeeeeeiiiiiooooooooooooooooouuuuuuuuuuuyyyyy'))
                  LIKE CONCAT('%', LOWER(FUNCTION('TRANSLATE', :keyword,
                   'áàảãạăắằẳẵặâấầẩẫậđéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵ',
                   'aaaaaaaaaaaaaaaaadeeeeeeeeeeeiiiiiooooooooooooooooouuuuuuuuuuuyyyyy')), '%')
               OR LOWER(e.email)    LIKE LOWER(CONCAT('%',:keyword,'%'))
               OR LOWER(c.title)    LIKE LOWER(CONCAT('%',:keyword,'%')))
        """)
    Page<Enrollment> findAllWithFilter(
            @Param("courseId") Long courseId,
            @Param("userId")   Long userId,
            @Param("status")   EnrollmentStatus status,
            @Param("keyword")  String keyword,
            Pageable pageable);

    @Query(value = """
        SELECT e FROM Enrollment e
        JOIN FETCH e.course c
        JOIN FETCH e.user u
        LEFT JOIN FETCH c.category
        WHERE c.instructor.id = :instructorId
          AND (:courseId IS NULL OR c.id = :courseId)
          AND (:status   IS NULL OR e.status = :status)
          AND (:keyword  IS NULL
               OR LOWER(e.fullName) LIKE LOWER(CONCAT('%',:keyword,'%'))
             OR LOWER(FUNCTION('TRANSLATE', e.fullName,
               'áàảãạăắằẳẵặâấầẩẫậđéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵ',
               'aaaaaaaaaaaaaaaaadeeeeeeeeeeeiiiiiooooooooooooooooouuuuuuuuuuuyyyyy'))
              LIKE CONCAT('%', LOWER(FUNCTION('TRANSLATE', :keyword,
               'áàảãạăắằẳẵặâấầẩẫậđéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵ',
               'aaaaaaaaaaaaaaaaadeeeeeeeeeeeiiiiiooooooooooooooooouuuuuuuuuuuyyyyy')), '%')
               OR LOWER(e.email)    LIKE LOWER(CONCAT('%',:keyword,'%'))
               OR LOWER(c.title)    LIKE LOWER(CONCAT('%',:keyword,'%')))
        """,
            countQuery = """
        SELECT COUNT(e) FROM Enrollment e
        JOIN e.course c JOIN e.user u
        WHERE c.instructor.id = :instructorId
          AND (:courseId IS NULL OR c.id = :courseId)
          AND (:status   IS NULL OR e.status = :status)
          AND (:keyword  IS NULL
               OR LOWER(e.fullName) LIKE LOWER(CONCAT('%',:keyword,'%'))
               OR LOWER(FUNCTION('TRANSLATE', e.fullName,
                   'áàảãạăắằẳẵặâấầẩẫậđéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵ',
                   'aaaaaaaaaaaaaaaaadeeeeeeeeeeeiiiiiooooooooooooooooouuuuuuuuuuuyyyyy'))
                  LIKE CONCAT('%', LOWER(FUNCTION('TRANSLATE', :keyword,
                   'áàảãạăắằẳẵặâấầẩẫậđéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵ',
                   'aaaaaaaaaaaaaaaaadeeeeeeeeeeeiiiiiooooooooooooooooouuuuuuuuuuuyyyyy')), '%')
               OR LOWER(e.email)    LIKE LOWER(CONCAT('%',:keyword,'%'))
               OR LOWER(c.title)    LIKE LOWER(CONCAT('%',:keyword,'%')))
        """)
    Page<Enrollment> findByInstructorWithFilter(
            @Param("instructorId") Long instructorId,
            @Param("courseId")     Long courseId,
            @Param("status")       EnrollmentStatus status,
            @Param("keyword")      String keyword,
            Pageable pageable);


    @Query("""
        SELECT e FROM Enrollment e
        JOIN FETCH e.course c
        JOIN FETCH e.user u
        LEFT JOIN FETCH c.category
        LEFT JOIN FETCH c.instructor
        WHERE e.id = :id
        """)
    Optional<Enrollment> findByIdWithDetails(@Param("id") Long id);

    @Query("""
        SELECT CASE WHEN COUNT(e) > 0 THEN TRUE ELSE FALSE END
        FROM Enrollment e
        WHERE e.id = :enrollmentId
          AND e.course.instructor.id = :instructorId
        """)
    boolean existsByIdAndInstructorId(
            @Param("enrollmentId") Long enrollmentId,
            @Param("instructorId") Long instructorId);

    @Query("""
        SELECT e FROM Enrollment e
        JOIN FETCH e.course c
        JOIN FETCH e.user u
        WHERE (:courseId IS NULL OR c.id = :courseId)
          AND (:status   IS NULL OR e.status = :status)
        ORDER BY e.enrollDate DESC
        """)
    List<Enrollment> findAllForExport(
            @Param("courseId") Long courseId,
            @Param("status")   EnrollmentStatus status);

    @Query("""
        SELECT e FROM Enrollment e
        JOIN FETCH e.course c
        JOIN FETCH e.user u
        WHERE c.instructor.id = :instructorId
          AND (:courseId IS NULL OR c.id = :courseId)
          AND (:status   IS NULL OR e.status = :status)
        ORDER BY e.enrollDate DESC
        """)
    List<Enrollment> findAllForExportByInstructor(
            @Param("instructorId") Long instructorId,
            @Param("courseId") Long courseId,
            @Param("status") EnrollmentStatus status);

    @Query("""
      SELECT DISTINCT u FROM Enrollment e
      JOIN e.user u
      ORDER BY u.fullName ASC, u.id ASC
      """)
    List<User> findDistinctUsersForFilter();

    @Query("""
      SELECT DISTINCT u FROM Enrollment e
      JOIN e.user u
      WHERE e.course.instructor.id = :instructorId
      ORDER BY u.fullName ASC, u.id ASC
      """)
    List<User> findDistinctUsersForFilterByInstructor(@Param("instructorId") Long instructorId);
}