package com.online.lms.controller.enrollments;

import com.online.lms.dto.enrollment.EnrollmentRequestDTO;
import com.online.lms.entity.Course;
import com.online.lms.enums.EnrollmentStatus;
import com.online.lms.exceptions.ResourceNotFoundException;
import com.online.lms.service.CourseService;
import com.online.lms.service.EnrollmentService;
import com.online.lms.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/enroll")
@PreAuthorize("isAuthenticated()")
public class EnrollCourseController {

    private final EnrollmentService enrollmentService;
    private final CourseService     courseService;
    private final UserService       userService;

    @GetMapping("/{courseId}")
    public String showEnrollForm(@PathVariable Long courseId, Model model,
                                 RedirectAttributes ra) {
        log.info("GET /enroll/{}", courseId);

        Course course;
        try {
            course = courseService.findById(courseId);
        } catch (ResourceNotFoundException e) {
            ra.addFlashAttribute("errorMessage", "Không tìm thấy khóa học.");
            return "redirect:/courses";
        }

        Optional<EnrollmentStatus> existing = enrollmentService.getExistingEnrollmentStatus(courseId);
        if (existing.isPresent()) {
            EnrollmentStatus s = existing.get();
            if (s == EnrollmentStatus.APPROVED) {
                ra.addFlashAttribute("successMessage", "Bạn đã được duyệt vào khóa học này. Hãy bắt đầu học!");
                return "redirect:/my-courses";
            }
            if (s == EnrollmentStatus.PENDING) {
                ra.addFlashAttribute("successMessage", "Bạn đã đăng ký khóa học này rồi. Đang chờ duyệt.");
                return "redirect:/my-enrollments";
            }
        }

        var currentUser = userService.getCurrentUser();
        var dto = new EnrollmentRequestDTO();
        dto.setFullName(currentUser.getFullName());
        dto.setEmail(currentUser.getEmail());

        model.addAttribute("course", course);
        model.addAttribute("enrollmentForm", dto);
        model.addAttribute("currentPage", "courses");
        return "enrollment/enroll-form";
    }

    @PostMapping("/{courseId}")
    public String submitEnroll(@PathVariable Long courseId,
                               @Valid @ModelAttribute("enrollmentForm") EnrollmentRequestDTO dto,
                               BindingResult result,
                               Model model,
                               RedirectAttributes ra) {
        log.info("POST /enroll/{}", courseId);

        Course course;
        try {
            course = courseService.findById(courseId);
        } catch (ResourceNotFoundException e) {
            ra.addFlashAttribute("errorMessage", "Không tìm thấy khóa học.");
            return "redirect:/courses";
        }

        if (result.hasErrors()) {
            model.addAttribute("course", course);
            model.addAttribute("currentPage", "courses");
            return "enrollment/enroll-form";
        }

        try {
            Long enrollmentId = enrollmentService.enroll(courseId, dto);

            // Only redirect to VNPay if user explicitly chose VNPay payment
            if (dto.getPaymentMethod() != null &&
                    dto.getPaymentMethod().name().equals("VNPAY")) {
                return "redirect:/payment/vnpay/initiate/" + enrollmentId;
            }

            // Manual bank transfer / internet banking → wait for admin approval
            ra.addFlashAttribute("successMessage",
                    "Đăng ký thành công! Vui lòng chờ quản trị viên xét duyệt và liên hệ thanh toán.");
            return "redirect:/my-enrollments";
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/my-enrollments";
        } catch (Exception e) {
            log.error("Enroll error for courseId={}", courseId, e);
            ra.addFlashAttribute("errorMessage", "Có lỗi xảy ra. Vui lòng thử lại.");
            return "redirect:/courses/" + courseId;
        }
    }
}
