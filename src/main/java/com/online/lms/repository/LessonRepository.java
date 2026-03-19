package com.online.lms.repository;

import com.online.lms.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    int countByChapterId(Long chapterId);

        Optional<Lesson> findByIdAndChapter_Course_IdAndStatusTrue(Long lessonId, Long courseId);

        @Query("""
                        SELECT l FROM Lesson l
                        JOIN l.chapter ch
                        WHERE ch.course.id = :courseId
                            AND l.status = true
                        ORDER BY ch.orderIndex ASC, l.orderIndex ASC, l.id ASC
                        """)
        List<Lesson> findActiveLessonsByCourseOrder(@Param("courseId") Long courseId);
}
