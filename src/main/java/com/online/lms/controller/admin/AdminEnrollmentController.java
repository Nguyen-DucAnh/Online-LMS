package com.online.lms.controller.admin;

import com.online.lms.constant.EnrollmentViewNames;
import com.online.lms.dto.enrollment.EnrollmentFormDTO;
import com.online.lms.entity.Course;
import com.online.lms.enums.EnrollmentStatus;
import com.online.lms.service.AdminEnrollmentService;
import com.online.lms.service.CourseService;
import com.online.lms.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/admin/enrollments")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class AdminEnrollmentController {

    private final AdminEnrollmentService enrollmentService;
    private final CourseService          courseService;
    private final UserService            userService;

    @GetMapping
    public String list(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth,
            Model model) {

        log.info("Hits GET /admin/enrollments");

        EnrollmentStatus statusEnum = parseStatus(status);
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "enrollDate"));

        if (isAdmin(auth)) {
            model.addAttribute("enrollments",
                    enrollmentService.findAll(courseId, userId, statusEnum, keyword, pageable));
        } else {
            Long instructorId = userService.getCurrentUser().getId();
            model.addAttribute("enrollments",
                    enrollmentService.findByInstructor(instructorId, courseId, statusEnum, keyword, pageable));
        }

        populateListModel(model, courseId, userId, status, keyword, auth);
        return EnrollmentViewNames.ENROLLMENT_LIST;
    }

    // ── New form ──────────────────────────────────────────────────────────────

    @GetMapping("/new")
    public String newForm(Authentication auth, Model model) {
        log.info("Hits GET /admin/enrollments/new");
        model.addAttribute("enrollmentForm", new EnrollmentFormDTO());
        populateFormModel(model, auth);
        return EnrollmentViewNames.ENROLLMENT_FORM;
    }

    // ── Edit form ─────────────────────────────────────────────────────────────

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Authentication auth, Model model) {
        log.info("Hits GET /admin/enrollments/{}/edit", id);
        checkManagerAccess(id, auth);
        model.addAttribute("enrollmentForm", enrollmentService.findFormById(id));
        populateFormModel(model, auth);
        return EnrollmentViewNames.ENROLLMENT_FORM;
    }

    // ── Save (tạo mới hoặc cập nhật) ─────────────────────────────────────────

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("enrollmentForm") EnrollmentFormDTO dto,
                       BindingResult result,
                       Authentication auth,
                       Model model,
                       RedirectAttributes ra) {

        if (result.hasErrors()) {
            populateFormModel(model, auth);
            return EnrollmentViewNames.ENROLLMENT_FORM;
        }

        // Manager không được edit enrollment của course người khác
        if (dto.getId() != null) checkManagerAccess(dto.getId(), auth);
        if (dto.getId() == null && !isAdmin(auth)) {
            checkManagerCourseAccess(dto.getCourseId(), auth);
        }

        try {
            enrollmentService.save(dto);
            ra.addFlashAttribute("successMessage",
                    dto.getId() == null ? "Tạo enrollment thành công!" : "Cập nhật thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/enrollments/" +
                    (dto.getId() != null ? dto.getId() + "/edit" : "new");
        }
        return "redirect:/admin/enrollments";
    }

    // ── Approve ───────────────────────────────────────────────────────────────

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        log.info("POST /admin/enrollments/{}/approve", id);
        checkManagerAccess(id, auth);
        try {
            enrollmentService.approve(id);
            ra.addFlashAttribute("successMessage", "Đã phê duyệt enrollment!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/enrollments";
    }

    // ── Reject ────────────────────────────────────────────────────────────────

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id,
                         @RequestParam(defaultValue = "") String rejectedNotes,
                         Authentication auth,
                         RedirectAttributes ra) {
        log.info("POST /admin/enrollments/{}/reject", id);
        checkManagerAccess(id, auth);
        try {
            enrollmentService.reject(id, rejectedNotes);
            ra.addFlashAttribute("successMessage", "Đã từ chối enrollment.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/enrollments";
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        log.info("POST /admin/enrollments/{}/delete", id);
        checkManagerAccess(id, auth);
        try {
            enrollmentService.delete(id);
            ra.addFlashAttribute("successMessage", "Đã xóa enrollment!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/enrollments";
    }

    // ── Export CSV ────────────────────────────────────────────────────────────

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String status,
            Authentication auth) throws IOException {

        log.info("GET /admin/enrollments/export");
        byte[] data;
        if (isAdmin(auth)) {
            data = enrollmentService.exportToCsv(courseId, parseStatus(status));
        } else {
            Long instructorId = userService.getCurrentUser().getId();
            if (courseId != null) {
                checkManagerCourseAccess(courseId, auth);
            }
            data = enrollmentService.exportToCsvByInstructor(instructorId, courseId, parseStatus(status));
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"enrollments.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(data);
    }

    // ── Import Excel ──────────────────────────────────────────────────────────

    @PostMapping("/import")
    public String importExcel(
            @RequestParam Long courseId,
            @RequestParam MultipartFile file,
            Authentication auth,
            RedirectAttributes ra) {

        log.info("POST /admin/enrollments/import, courseId={}", courseId);
        if (file.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Vui lòng chọn file Excel.");
            return "redirect:/admin/enrollments";
        }
        if (!isAdmin(auth)) {
            checkManagerCourseAccess(courseId, auth);
        }
        try {
            int count = enrollmentService.importFromExcel(courseId, file);
            ra.addFlashAttribute("successMessage",
                    "Import thành công " + count + " enrollment!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi import: " + e.getMessage());
        }
        return "redirect:/admin/enrollments";
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * Manager chỉ được xem/sửa enrollment của course mình.
     * Dùng EnrollmentRepository.existsByIdAndInstructorId để check thực sự.
     */
    private void checkManagerAccess(Long enrollmentId, Authentication auth) {
        if (isAdmin(auth)) return;
        Long managerId = userService.getCurrentUser().getId();
        boolean hasAccess = enrollmentService.hasManagerAccess(enrollmentId, managerId);
        if (!hasAccess) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Bạn không có quyền truy cập enrollment này.");
        }
    }

    private void checkManagerCourseAccess(Long courseId, Authentication auth) {
        if (isAdmin(auth)) return;
        if (courseId == null) {
            throw new org.springframework.security.access.AccessDeniedException("Course không hợp lệ.");
        }
        Long managerId = userService.getCurrentUser().getId();
        Course course = courseService.findById(courseId);
        boolean hasAccess = course.getInstructor() != null && managerId.equals(course.getInstructor().getId());
        if (!hasAccess) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Bạn không có quyền thao tác enrollment cho khóa học này.");
        }
    }

    private EnrollmentStatus parseStatus(String status) {
        if (status == null || status.isBlank()) return null;
        try { return EnrollmentStatus.valueOf(status.toUpperCase()); }
        catch (IllegalArgumentException e) { return null; }
    }

    private void populateListModel(Model model, Long courseId, Long userId,
                                   String status, String keyword,
                                   Authentication auth) {
        model.addAttribute("courses",  getAvailableCourses(auth));
        if (isAdmin(auth)) {
            model.addAttribute("users", enrollmentService.findFilterUsers());
        } else {
            Long instructorId = userService.getCurrentUser().getId();
            model.addAttribute("users", enrollmentService.findFilterUsersByInstructor(instructorId));
        }
        model.addAttribute("statuses", EnrollmentStatus.values());
        model.addAttribute("selectedCourseId", courseId);
        model.addAttribute("selectedUserId",   userId);
        model.addAttribute("selectedStatus",   status);
        model.addAttribute("keyword",          keyword);
        model.addAttribute("currentPage",      "enrollments");
    }

    private void populateFormModel(Model model, Authentication auth) {
        model.addAttribute("courses",  getAvailableCourses(auth));
        model.addAttribute("statuses", EnrollmentStatus.values());
        model.addAttribute("currentPage", "enrollments");
    }

    private List<Course> getAvailableCourses(Authentication auth) {
        if (isAdmin(auth)) {
            return courseService.findAll();
        }
        Long managerId = userService.getCurrentUser().getId();
        return courseService.findAll().stream()
                .filter(c -> c.getInstructor() != null && managerId.equals(c.getInstructor().getId()))
                .toList();
    }
}