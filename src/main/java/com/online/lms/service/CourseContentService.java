package com.online.lms.service;

import com.online.lms.dto.chapter.ChapterDTO;
import com.online.lms.dto.chapter.LessonDTO;
import com.online.lms.entity.Chapter;
import com.online.lms.entity.Lesson;

import java.util.List;

public interface CourseContentService {

    List<ChapterDTO> findChaptersByCourse(Long courseId);

    List<ChapterDTO> findActiveChaptersByCourse(Long courseId);

    LessonDTO findActiveLessonByCourseAndId(Long courseId, Long lessonId);

    LessonDTO findFirstActiveLessonByCourse(Long courseId);

    void saveChapter(ChapterDTO dto);

    void deleteChapter(Long chapterId);

    void saveLesson(LessonDTO dto);

    void toggleLessonStatus(Long lessonId);

    void deleteLesson(Long lessonId);

    LessonDTO findLessonById(Long lessonId);

    ChapterDTO findChapterById(Long chapterId);

    Chapter findChapterEntity(Long chapterId, Long courseId);

    Lesson findLessonEntity(Long lessonId, Long chapterId);
}
