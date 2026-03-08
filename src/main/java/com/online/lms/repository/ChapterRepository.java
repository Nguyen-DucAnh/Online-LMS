package com.online.lms.repository;

import com.online.lms.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {

    @Query("SELECT ch FROM Chapter ch LEFT JOIN FETCH ch.lessons l WHERE ch.course.id = :courseId ORDER BY ch.orderIndex ASC")
    List<Chapter> findByCourseIdWithLessons(@Param("courseId") Long courseId);

    Long countByCourseId(Long courseId);
}
