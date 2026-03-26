package com.online.lms.service;

import com.online.lms.dto.payment.VNPayResponseDTO;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.util.Map;

public interface VNPayService {

    /**
     * Tạo URL thanh toán VNPay cho một enrollment.
     *
     * @param enrollmentId  ID của enrollment cần thanh toán
     * @param amount        Học phí (VNĐ)
     * @param courseTitle   Tên khóa học (ghi vào orderInfo)
     * @param httpRequest   HttpServletRequest để lấy IP
     * @return DTO chứa paymentUrl và code
     */
    VNPayResponseDTO createPaymentUrl(Long enrollmentId, BigDecimal amount,
                                      String courseTitle, HttpServletRequest httpRequest);

    /**
     * Xử lý kết quả VNPay redirect về (vnp_ResponseCode, vnp_SecureHash...).
     */
    VNPayResponseDTO processPaymentReturn(Map<String, String> params);
}
