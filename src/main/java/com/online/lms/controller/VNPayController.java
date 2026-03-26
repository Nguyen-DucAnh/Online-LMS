package com.online.lms.controller;

import com.online.lms.dto.payment.VNPayResponseDTO;
import com.online.lms.entity.Enrollment;
import com.online.lms.enums.EnrollmentStatus;
import com.online.lms.exceptions.ResourceNotFoundException;
import com.online.lms.repository.EnrollmentRepository;
import com.online.lms.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Handles VNPay payment flow for course enrollment.
 * GET /payment/vnpay/initiate/{enrollmentId} → redirect to VNPay
 * GET /payment/vnpay/return                  → callback from VNPay (public — VNPay redirects here)
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/payment/vnpay")
public class VNPayController {

    private final VNPayService          vnPayService;
    private final EnrollmentRepository  enrollmentRepository;

    /**
     * Initiate payment — requires login.
     */
    @GetMapping("/initiate/{enrollmentId}")
    @PreAuthorize("isAuthenticated()")
    public String initiatePayment(@PathVariable Long enrollmentId,
                                  HttpServletRequest request,
                                  RedirectAttributes ra) {
        log.info("VNPay initiate - enrollmentId={}", enrollmentId);

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found: " + enrollmentId));

        BigDecimal fee = enrollment.getFee() != null ? enrollment.getFee() : BigDecimal.ZERO;

        // Free course — no payment needed
        if (fee.compareTo(BigDecimal.ZERO) == 0) {
            // Auto-approve free courses
            enrollment.setStatus(EnrollmentStatus.APPROVED);
            enrollmentRepository.save(enrollment);
            ra.addFlashAttribute("successMessage",
                    "Đăng ký thành công! Khóa học miễn phí — bạn có thể bắt đầu học ngay!");
            return "redirect:/my-courses";
        }

        String courseTitle = enrollment.getCourse().getTitle();
        VNPayResponseDTO response = vnPayService.createPaymentUrl(enrollmentId, fee, courseTitle, request);

        if (!"00".equals(response.getCode())) {
            ra.addFlashAttribute("errorMessage", "Không thể khởi tạo thanh toán: " + response.getMessage());
            return "redirect:/my-enrollments";
        }

        return "redirect:" + response.getPaymentUrl();
    }

    /**
     * VNPay return callback — PUBLIC (VNPay redirects here, no session guarantee).
     * Verifies HMAC, auto-approves enrollment on success, shows result page.
     */
    @GetMapping("/return")
    public String paymentReturn(@RequestParam Map<String, String> params, Model model) {
        log.info("VNPay return - responseCode={}", params.get("vnp_ResponseCode"));

        VNPayResponseDTO response = vnPayService.processPaymentReturn(params);
        boolean success = "00".equals(response.getCode());

        Long courseId = null;

        if (response.getTxnRef() != null) {
            try {
                String idPart = response.getTxnRef().contains("_")
                        ? response.getTxnRef().split("_")[0]
                        : response.getTxnRef();
                Long enrollmentId = Long.parseLong(idPart);

                Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElse(null);
                if (enrollment != null) {
                    courseId = enrollment.getCourse().getId();

                    if (success) {
                        // Save VNPay transaction and AUTO-APPROVE so user can start immediately
                        enrollment.setVnpayTransactionNo(response.getTransactionNo());
                        enrollment.setStatus(EnrollmentStatus.APPROVED);
                        enrollmentRepository.save(enrollment);
                        log.info("Enrollment {} APPROVED after VNPay payment txn={}",
                                enrollmentId, response.getTransactionNo());
                    } else {
                        log.info("VNPay payment failed for enrollment {} - keeping PENDING", enrollmentId);
                    }
                }
            } catch (NumberFormatException e) {
                log.warn("Could not parse enrollmentId from txnRef: {}", response.getTxnRef());
            }
        }

        model.addAttribute("vnpayResult", response);
        model.addAttribute("success", success);
        model.addAttribute("courseId", courseId);
        model.addAttribute("currentPage", "courses");
        return "enrollment/payment-result";
    }
}
