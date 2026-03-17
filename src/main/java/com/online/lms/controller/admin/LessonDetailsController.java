package com.online.lms.controller.admin;

import com.online.lms.dto.chapter.LessonDTO;
import com.online.lms.entity.Chapter;
import com.online.lms.entity.Course;
import com.online.lms.enums.LessonType;
import com.online.lms.service.CourseContentService;
import com.online.lms.service.CourseService;
import com.online.lms.service.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/courses/{courseId}/chapters/{chapterId}/lessons")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class LessonDetailsController {

    private final CourseContentService contentService;
    private final CourseService courseService;
    private final FileStorageService fileStorageService;

    @GetMapping("/new")
    public String showCreateForm(@PathVariable Long courseId,
                                 @PathVariable Long chapterId,
                                 Model model) {
        Course course = courseService.findById(courseId);
        Chapter chapter = contentService.findChapterEntity(chapterId, courseId);
        int nextOrder = contentService.findChaptersByCourse(courseId).stream()
                .filter(ch -> ch.getId().equals(chapterId))
                .findFirst()
                .map(ch -> ch.getLessons().size() + 1)
                .orElse(1);
        LessonDTO dto = LessonDTO.builder()
                .chapterId(chapterId)
                .type(LessonType.VIDEO)
                .duration(0)
                .status(true)
                .orderIndex(nextOrder)
                .previewEnabled(false)
                .build();
        model.addAttribute("course", course);
        model.addAttribute("chapter", chapter);
        model.addAttribute("lesson", dto);
        model.addAttribute("lessonTypes", LessonType.values());
        model.addAttribute("currentPage", "courses");
        return "admin/lesson-details";
    }

    @GetMapping("/{lessonId}/edit")
    public String showEditForm(@PathVariable Long courseId,
                               @PathVariable Long chapterId,
                               @PathVariable Long lessonId,
                               Model model) {
        Course course = courseService.findById(courseId);
        Chapter chapter = contentService.findChapterEntity(chapterId, courseId);
        LessonDTO lesson = contentService.findLessonById(lessonId);
        if (!lesson.getChapterId().equals(chapterId)) {
            return "redirect:/admin/courses/" + courseId + "/content";
        }
        model.addAttribute("course", course);
        model.addAttribute("chapter", chapter);
        model.addAttribute("lesson", lesson);
        model.addAttribute("lessonTypes", LessonType.values());
        model.addAttribute("currentPage", "courses");
        return "admin/lesson-details";
    }

    @PostMapping("/new")
    public String createLesson(@PathVariable Long courseId,
                               @PathVariable Long chapterId,
                               @Valid @ModelAttribute("lesson") LessonDTO dto,
                               BindingResult bindingResult,
                               @RequestParam(value = "videoFile", required = false) MultipartFile videoFile,
                               @RequestParam(value = "pdfFile", required = false) MultipartFile pdfFile,
                               Model model,
                               RedirectAttributes ra) {
        dto.setChapterId(chapterId);
        validateLessonContent(dto, videoFile, pdfFile, bindingResult);
        if (bindingResult.hasErrors()) {
            populateModelForError(model, courseId, chapterId);
            return "admin/lesson-details";
        }
        processFileUpload(dto, videoFile, pdfFile);
        contentService.saveLesson(dto);
        ra.addFlashAttribute("successMessage", "Đã tạo bài học thành công!");
        return "redirect:/admin/courses/" + courseId + "/content";
    }

    @PostMapping("/{lessonId}/edit")
    public String updateLesson(@PathVariable Long courseId,
                               @PathVariable Long chapterId,
                               @PathVariable Long lessonId,
                               @Valid @ModelAttribute("lesson") LessonDTO dto,
                               BindingResult bindingResult,
                               @RequestParam(value = "videoFile", required = false) MultipartFile videoFile,
                               @RequestParam(value = "pdfFile", required = false) MultipartFile pdfFile,
                               Model model,
                               RedirectAttributes ra) {
        dto.setId(lessonId);
        dto.setChapterId(chapterId);
        LessonDTO existing = contentService.findLessonById(lessonId);
        if (dto.getContentFilePath() == null || dto.getContentFilePath().isBlank()) {
            dto.setContentFilePath(existing.getContentFilePath());
        }
        validateLessonContentForUpdate(dto, videoFile, pdfFile, bindingResult);
        if (bindingResult.hasErrors()) {
            populateModelForError(model, courseId, chapterId);
            return "admin/lesson-details";
        }
        processFileUpload(dto, videoFile, pdfFile);
        contentService.saveLesson(dto);
        ra.addFlashAttribute("successMessage", "Đã cập nhật bài học!");
        return "redirect:/admin/courses/" + courseId + "/content";
    }

    private void validateLessonContent(LessonDTO dto, MultipartFile videoFile, MultipartFile pdfFile, BindingResult bindingResult) {
        if (dto.getType() == LessonType.VIDEO) {
            if ((videoFile == null || videoFile.isEmpty()) && (dto.getContentFilePath() == null || dto.getContentFilePath().isBlank())) {
                bindingResult.reject("file.required", "Vui lòng upload file video.");
            }
        } else if (dto.getType() == LessonType.PDF) {
            if ((pdfFile == null || pdfFile.isEmpty()) && (dto.getContentFilePath() == null || dto.getContentFilePath().isBlank())) {
                bindingResult.reject("file.required", "Vui lòng upload file PDF.");
            }
        }
    }

    private void validateLessonContentForUpdate(LessonDTO dto, MultipartFile videoFile, MultipartFile pdfFile, BindingResult bindingResult) {
        if (dto.getType() == LessonType.VIDEO) {
            if ((videoFile == null || videoFile.isEmpty()) && (dto.getContentFilePath() == null || dto.getContentFilePath().isBlank())) {
                bindingResult.reject("file.required", "Vui lòng upload file video.");
            }
        } else if (dto.getType() == LessonType.PDF) {
            if ((pdfFile == null || pdfFile.isEmpty()) && (dto.getContentFilePath() == null || dto.getContentFilePath().isBlank())) {
                bindingResult.reject("file.required", "Vui lòng upload file PDF.");
            }
        }
    }

    private void processFileUpload(LessonDTO dto, MultipartFile videoFile, MultipartFile pdfFile) {
        if (dto.getType() == LessonType.VIDEO && videoFile != null && !videoFile.isEmpty()) {
            dto.setContentFilePath(fileStorageService.store(videoFile, "videos"));
        } else if (dto.getType() == LessonType.PDF && pdfFile != null && !pdfFile.isEmpty()) {
            dto.setContentFilePath(fileStorageService.store(pdfFile, "pdfs"));
        } else if (dto.getType() == LessonType.TEXT) {
            dto.setContentFilePath(null);
        }
    }

    private void populateModelForError(Model model, Long courseId, Long chapterId) {
        model.addAttribute("course", courseService.findById(courseId));
        model.addAttribute("chapter", contentService.findChapterEntity(chapterId, courseId));
        model.addAttribute("lessonTypes", LessonType.values());
        model.addAttribute("currentPage", "courses");
    }
}
