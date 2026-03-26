package com.online.lms.controller.admin;

import com.online.lms.dto.chapter.LessonDTO;
import com.online.lms.entity.Chapter;
import com.online.lms.entity.Course;
import com.online.lms.entity.User;
import com.online.lms.enums.LessonType;
import com.online.lms.enums.UserRole;
import com.online.lms.repository.UserRepository;
import com.online.lms.service.CourseContentService;
import com.online.lms.service.CourseService;
import com.online.lms.service.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @ModelAttribute("allLessonTypes")
    public LessonType[] populateLessonTypes() {
        return LessonType.values();
    }

    @GetMapping("/new")
    public String showCreateForm(@PathVariable Long courseId,
                                 @PathVariable Long chapterId,
                                 Model model) {
        User currentUser = getCurrentUser();
        Course course = courseService.findById(courseId);
        if (currentUser.getRole() == UserRole.MANAGER && !course.getInstructor().getId().equals(currentUser.getId())) {
            return "redirect:/admin/courses";
        }

        LessonDTO dto = LessonDTO.builder()
                .chapterId(chapterId)
                .orderIndex(contentService.findChapterById(chapterId).getLessons().size() + 1)
                .type(LessonType.VIDEO)
                .duration(0)
                .status(true)
                .previewEnabled(false)
                .build();
        model.addAttribute("course", course);
        model.addAttribute("courseId", courseId);
        model.addAttribute("chapter", contentService.findChapterById(chapterId));
        model.addAttribute("lesson", dto);
        model.addAttribute("allLessonTypes", LessonType.values()); // Explicitly add
        model.addAttribute("currentPage", "courses");
        return "admin/lesson-details";
    }

    @GetMapping("/{lessonId}/edit")
    public String showEditForm(@PathVariable Long courseId,
                               @PathVariable Long chapterId,
                               @PathVariable Long lessonId,
                               Model model) {
        User currentUser = getCurrentUser();
        Course course = courseService.findById(courseId);
        if (currentUser.getRole() == UserRole.MANAGER && !course.getInstructor().getId().equals(currentUser.getId())) {
            return "redirect:/admin/courses";
        }

        LessonDTO lesson = contentService.findLessonById(lessonId);
        if (!lesson.getChapterId().equals(chapterId)) {
            return "redirect:/admin/courses/" + courseId + "/content";
        }
        model.addAttribute("course", course);
        model.addAttribute("courseId", courseId);
        model.addAttribute("chapter", contentService.findChapterById(chapterId));
        model.addAttribute("lesson", lesson);
        model.addAttribute("allLessonTypes", LessonType.values());
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
                               @RequestParam(value = "docxFile", required = false) MultipartFile docxFile,
                               @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                               Model model,
                               RedirectAttributes ra) {
        User currentUser = getCurrentUser();
        Course course = courseService.findById(courseId);
        if (currentUser.getRole() == UserRole.MANAGER && !course.getInstructor().getId().equals(currentUser.getId())) {
            return "redirect:/admin/courses";
        }

        dto.setChapterId(chapterId);
        validateLessonContent(dto, videoFile, pdfFile, docxFile, imageFile, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("course", course);
            model.addAttribute("courseId", courseId);
            model.addAttribute("chapter", contentService.findChapterById(chapterId));
            model.addAttribute("allLessonTypes", LessonType.values());
            model.addAttribute("currentPage", "courses");
            return "admin/lesson-details";
        }
        processFileUpload(dto, videoFile, pdfFile, docxFile, imageFile);
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
                               @RequestParam(value = "docxFile", required = false) MultipartFile docxFile,
                               @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                               Model model,
                               RedirectAttributes ra) {
        User currentUser = getCurrentUser();
        Course course = courseService.findById(courseId);
        if (currentUser.getRole() == UserRole.MANAGER && !course.getInstructor().getId().equals(currentUser.getId())) {
            return "redirect:/admin/courses";
        }

        dto.setId(lessonId);
        dto.setChapterId(chapterId);
        LessonDTO existing = contentService.findLessonById(lessonId);
        if (dto.getContentFilePath() == null || dto.getContentFilePath().isBlank()) {
            dto.setContentFilePath(existing.getContentFilePath());
        }
        validateLessonContentForUpdate(dto, videoFile, pdfFile, docxFile, imageFile, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("course", course);
            model.addAttribute("courseId", courseId);
            model.addAttribute("chapter", contentService.findChapterById(chapterId));
            model.addAttribute("allLessonTypes", LessonType.values());
            model.addAttribute("currentPage", "courses");
            return "admin/lesson-details";
        }
        processFileUpload(dto, videoFile, pdfFile, docxFile, imageFile);
        contentService.saveLesson(dto);
        ra.addFlashAttribute("successMessage", "Đã cập nhật bài học!");
        return "redirect:/admin/courses/" + courseId + "/content";
    }

    private void validateLessonContent(LessonDTO dto, MultipartFile videoFile, MultipartFile pdfFile,
                                       MultipartFile docxFile, MultipartFile imageFile, BindingResult bindingResult) {
        if (dto.getType() == LessonType.VIDEO && !hasFile(videoFile) && isBlank(dto.getContentFilePath())) {
            bindingResult.reject("file.required", "Vui lòng upload file video.");
        } else if (dto.getType() == LessonType.PDF && !hasFile(pdfFile) && isBlank(dto.getContentFilePath())) {
            bindingResult.reject("file.required", "Vui lòng upload file PDF.");
        } else if (dto.getType() == LessonType.DOCX && !hasFile(docxFile) && isBlank(dto.getContentFilePath())) {
            bindingResult.reject("file.required", "Vui lòng upload file DOCX.");
        } else if (dto.getType() == LessonType.IMAGE && !hasFile(imageFile) && isBlank(dto.getContentFilePath())) {
            bindingResult.reject("file.required", "Vui lòng upload ảnh.");
        }
    }

    private void validateLessonContentForUpdate(LessonDTO dto, MultipartFile videoFile, MultipartFile pdfFile,
                                               MultipartFile docxFile, MultipartFile imageFile, BindingResult bindingResult) {
        validateLessonContent(dto, videoFile, pdfFile, docxFile, imageFile, bindingResult);
    }

    private boolean hasFile(MultipartFile f) {
        return f != null && !f.isEmpty();
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private void processFileUpload(LessonDTO dto, MultipartFile videoFile, MultipartFile pdfFile,
                                  MultipartFile docxFile, MultipartFile imageFile) {
        if (dto.getType() == LessonType.TEXT) {
            dto.setContentFilePath(null);
        } else if (dto.getType() == LessonType.VIDEO) {
            if (hasFile(videoFile)) {
                dto.setContentFilePath(fileStorageService.store(videoFile, "videos"));
            }
        } else if (dto.getType() == LessonType.PDF && hasFile(pdfFile)) {
            dto.setContentFilePath(fileStorageService.store(pdfFile, "pdfs"));
        } else if (dto.getType() == LessonType.DOCX && hasFile(docxFile)) {
            dto.setContentFilePath(fileStorageService.store(docxFile, "docx"));
        } else if (dto.getType() == LessonType.IMAGE && hasFile(imageFile)) {
            dto.setContentFilePath(fileStorageService.store(imageFile, "images"));
        }
    }


    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }
}
