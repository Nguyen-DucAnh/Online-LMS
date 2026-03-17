package com.online.lms.service.impl;

import com.online.lms.dto.chapter.ChapterDTO;
import com.online.lms.dto.chapter.LessonDTO;
import com.online.lms.entity.Chapter;
import com.online.lms.enums.LessonType;
import com.online.lms.entity.Course;
import com.online.lms.entity.Lesson;
import com.online.lms.exceptions.ResourceNotFoundException;
import com.online.lms.repository.ChapterRepository;
import com.online.lms.repository.CourseRepository;
import com.online.lms.repository.LessonRepository;
import com.online.lms.service.CourseContentService;
import com.online.lms.utils.CourseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Scope("singleton")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseContentServiceImpl implements CourseContentService {

    private final ChapterRepository chapterRepository;
    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;

    @Override
    public List<ChapterDTO> findChaptersByCourse(Long courseId) {
        return chapterRepository.findByCourseIdWithLessons(courseId)
                .stream().map(CourseMapper::toChapterDTO).toList();
    }

    @Override
    @Transactional
    public void saveChapter(ChapterDTO dto) {
        if (dto.getId() != null) {
            Chapter chapter = getChapterOrThrow(dto.getId());
            chapter.setTitle(dto.getTitle());
            chapter.setDescription(dto.getDescription());
            if (dto.getOrderIndex() != null) {
                chapter.setOrderIndex(dto.getOrderIndex());
            }
            chapterRepository.save(chapter);
            log.info("Chapter updated: id={}", dto.getId());
        } else {
            Course course = getCourseOrThrow(dto.getCourseId());
            Long order = dto.getOrderIndex() != null ? dto.getOrderIndex() : chapterRepository.countByCourseId(dto.getCourseId());
            Chapter chapter = Chapter.builder()
                    .title(dto.getTitle())
                    .description(dto.getDescription())
                    .orderIndex(order)
                    .course(course)
                    .build();
            chapterRepository.save(chapter);
            log.info("Chapter created for course id={}", dto.getCourseId());
        }
    }

    @Override
    @Transactional
    public void deleteChapter(Long chapterId) {
        chapterRepository.delete(getChapterOrThrow(chapterId));
        log.info("Chapter deleted: id={}", chapterId);
    }

    @Override
    @Transactional
    public void saveLesson(LessonDTO dto) {
        if (dto.getId() != null) {
            Lesson lesson = getLessonOrThrow(dto.getId());
            lesson.setTitle(dto.getTitle());
            lesson.setContent(dto.getContent());
            lesson.setType(dto.getType());
            lesson.setDuration(dto.getDuration() != null ? dto.getDuration() : 0);
            lesson.setOrderIndex(dto.getOrderIndex() != null ? dto.getOrderIndex() : 0);
            lesson.setStatus(dto.getStatus() != null ? dto.getStatus() : true);
            if (dto.getType() == LessonType.TEXT) {
                lesson.setContentFilePath(null);
            } else if (dto.getContentFilePath() != null) {
                lesson.setContentFilePath(dto.getContentFilePath());
            }
            if (dto.getPreviewEnabled() != null) {
                lesson.setPreviewEnabled(dto.getPreviewEnabled());
            }
            lessonRepository.save(lesson);
            log.info("Lesson updated: id={}", dto.getId());
        } else {
            Chapter chapter = getChapterOrThrow(dto.getChapterId());
            Lesson lesson = Lesson.builder()
                    .title(dto.getTitle())
                    .content(dto.getContent())
                    .type(dto.getType())
                    .duration(dto.getDuration() != null ? dto.getDuration() : 0)
                    .orderIndex(dto.getOrderIndex() != null ? dto.getOrderIndex() : lessonRepository.countByChapterId(dto.getChapterId()))
                    .status(dto.getStatus() != null ? dto.getStatus() : true)
                    .contentFilePath(dto.getContentFilePath())
                    .previewEnabled(dto.getPreviewEnabled() != null && dto.getPreviewEnabled())
                    .chapter(chapter)
                    .build();
            lessonRepository.save(lesson);
            log.info("Lesson created in chapter id={}", dto.getChapterId());
        }
    }

    @Override
    @Transactional
    public void toggleLessonStatus(Long lessonId) {
        Lesson lesson = getLessonOrThrow(lessonId);
        lesson.setStatus(!lesson.getStatus());
        lessonRepository.save(lesson);
        log.info("Lesson {} status toggled → {}", lessonId, lesson.getStatus());
    }

    @Override
    @Transactional
    public void deleteLesson(Long lessonId) {
        lessonRepository.delete(getLessonOrThrow(lessonId));
        log.info("Lesson deleted: id={}", lessonId);
    }

    @Override
    public LessonDTO findLessonById(Long lessonId) {
        return CourseMapper.toLessonDTO(getLessonOrThrow(lessonId));
    }

    @Override
    public ChapterDTO findChapterById(Long chapterId) {
        return CourseMapper.toChapterDTO(getChapterOrThrow(chapterId));
    }

    @Override
    public Chapter findChapterEntity(Long chapterId, Long courseId) {
        return chapterRepository.findByIdAndCourse_Id(chapterId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chương id=" + chapterId));
    }

    @Override
    public Lesson findLessonEntity(Long lessonId, Long chapterId) {
        Lesson lesson = getLessonOrThrow(lessonId);
        if (!lesson.getChapter().getId().equals(chapterId)) {
            throw new ResourceNotFoundException("Bài học không thuộc chương này");
        }
        return lesson;
    }

    // ===== Private helpers =====

    private Chapter getChapterOrThrow(Long id) {
        return chapterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chương id=" + id));
    }

    private Lesson getLessonOrThrow(Long id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài học id=" + id));
    }

    private Course getCourseOrThrow(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học id=" + id));
    }
}
