package com.online.lms.utils;

import com.online.lms.dto.chapter.ChapterDTO;
import com.online.lms.dto.chapter.LessonDTO;
import com.online.lms.dto.course.CourseFormDTO;
import com.online.lms.dto.course.CourseListItemDTO;
import com.online.lms.entity.Chapter;
import com.online.lms.entity.Course;
import com.online.lms.entity.Lesson;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CourseMapper {

    public static CourseListItemDTO toListItemDTO(Course c) {
        return CourseListItemDTO.builder()
                .id(c.getId())
                .title(c.getTitle())
                .thumbnail(c.getThumbnail())
                .categoryName(c.getCategory() != null ? c.getCategory().getName() : "—")
                .instructorName(c.getInstructor() != null ? c.getInstructor().getFullName() : "—")
                .instructorEmail(c.getInstructor() != null ? c.getInstructor().getEmail() : null)
                .listedPrice(c.getListedPrice())
                .status(c.getStatus())
                .level(c.getLevel())
                .featured(c.getFeatured())
                .chapterCount(c.getChapters() != null ? c.getChapters().size() : 0)
                .createdAt(c.getCreatedAt())
                .build();
    }

    public static CourseFormDTO toFormDTO(Course c) {
        return CourseFormDTO.builder()
                .id(c.getId())
                .title(c.getTitle())
                .description(c.getDescription())
                .thumbnail(c.getThumbnail())
                .categoryId(c.getCategory() != null ? c.getCategory().getId() : null)
                .instructorId(c.getInstructor() != null ? c.getInstructor().getId() : null)
                .level(c.getLevel())
                .listedPrice(c.getListedPrice())
                .salePrice(c.getSalePrice())
                .duration(c.getDuration())
                .status(c.getStatus())
                .featured(c.getFeatured())
                .build();
    }

    public static ChapterDTO toChapterDTO(Chapter ch) {
        List<LessonDTO> lessons = ch.getLessons() != null
                ? ch.getLessons().stream().map(CourseMapper::toLessonDTO).toList()
                : List.of();
        return ChapterDTO.builder()
                .id(ch.getId())
                .title(ch.getTitle())
                .description(ch.getDescription())
                .orderIndex(ch.getOrderIndex())
                .courseId(ch.getCourse().getId())
                .lessons(lessons)
                .build();
    }

    public static LessonDTO toLessonDTO(Lesson l) {
        return LessonDTO.builder()
                .id(l.getId())
                .title(l.getTitle())
                .content(l.getContent())
                .type(l.getType())
                .duration(l.getDuration())
                .status(l.getStatus())
                .orderIndex(l.getOrderIndex())
                .chapterId(l.getChapter().getId())
                .contentFilePath(l.getContentFilePath())
                .previewEnabled(l.getPreviewEnabled() != null && l.getPreviewEnabled())
                .build();
    }
}
