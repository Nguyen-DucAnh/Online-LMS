package com.online.lms.service;

import com.online.lms.dto.chapter.ChapterDTO;
import com.online.lms.dto.chapter.LessonDTO;

import java.util.List;

public interface CourseContentService {

    List<ChapterDTO> findChaptersByCourse(Long courseId);

    void saveChapter(ChapterDTO dto);

    void deleteChapter(Long chapterId);

    void saveLesson(LessonDTO dto);

    void toggleLessonStatus(Long lessonId);

    void deleteLesson(Long lessonId);
}
