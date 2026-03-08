package com.online.lms.service;

import com.online.lms.dto.chapter.ChapterDTO;
import com.online.lms.dto.chapter.LessonDTO;

import java.util.List;

public interface CourseContentService {

    List<ChapterDTO> findChaptersByCourse(int courseId);

    void saveChapter(ChapterDTO dto);

    void deleteChapter(int chapterId);

    void saveLesson(LessonDTO dto);

    void toggleLessonStatus(int lessonId);

    void deleteLesson(int lessonId);
}
