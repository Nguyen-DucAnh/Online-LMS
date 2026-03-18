package com.online.lms.repository;

import com.online.lms.entity.Course;
import com.online.lms.enums.CourseLevel;
import com.online.lms.enums.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    @Query("""
            SELECT c FROM Course c
            LEFT JOIN FETCH c.category cat
            LEFT JOIN FETCH c.instructor ins
            WHERE (:keyword IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:categoryId IS NULL OR cat.id = :categoryId)
              AND (:status IS NULL OR c.status = :status)
            """)
    Page<Course> search(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("status") CourseStatus status,
            Pageable pageable
    );

    @Query("""
            SELECT c FROM Course c
            LEFT JOIN FETCH c.category cat
            LEFT JOIN FETCH c.instructor ins
            WHERE c.status = 'PUBLISHED'
              AND (:keyword IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:categoryId IS NULL OR cat.id = :categoryId)
              AND (:level IS NULL OR c.level = :level)
            """)
    Page<Course> searchPublished(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("level") CourseLevel level,
            Pageable pageable
    );

    long countByStatus(CourseStatus status);
}
