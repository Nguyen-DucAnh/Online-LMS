package com.online.lms.repository;

import com.online.lms.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    int countByChapterId(Long chapterId);

    @org.springframework.data.jpa.repository.Query("SELECT l FROM Lesson l JOIN FETCH l.chapter ch JOIN FETCH ch.course WHERE l.id = :lessonId")
    java.util.Optional<Lesson> findByIdWithChapterAndCourse(@org.springframework.data.repository.query.Param("lessonId") Long lessonId);
}
