package com.online.lms.service.impl;

import com.online.lms.config.VNPayConfig;
import com.online.lms.dto.payment.VNPayResponseDTO;
import com.online.lms.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class VNPayServiceImpl implements VNPayService {

    private final VNPayConfig vnPayConfig;

    @Override
    public VNPayResponseDTO createPaymentUrl(Long enrollmentId, BigDecimal amount,
                                             String courseTitle, HttpServletRequest httpRequest) {
        try {
            // VNPay requires amount in VND * 100 (no decimals)
            long vnpAmount = amount.multiply(BigDecimal.valueOf(100)).longValue();

            // Unique TxnRef = enrollmentId_timestamp (avoids "duplicate order" rejection)
            String vnpTxnRef = enrollmentId + "_" + System.currentTimeMillis();

            String orderInfo = "Thanh toan khoa hoc " + courseTitle
                    .replaceAll("[^a-zA-Z0-9 ]", "")   // strip special chars VNPay dislikes
                    .trim();

            Map<String, String> params = new HashMap<>();
            params.put("vnp_Version",   vnPayConfig.getVersion());
            params.put("vnp_Command",   vnPayConfig.getCommand());
            params.put("vnp_TmnCode",   vnPayConfig.getTmnCode());
            params.put("vnp_Amount",    String.valueOf(vnpAmount));
            params.put("vnp_CurrCode",  "VND");
            params.put("vnp_TxnRef",    vnpTxnRef);
            params.put("vnp_OrderInfo", orderInfo);
            params.put("vnp_OrderType", vnPayConfig.getOrderType());
            params.put("vnp_Locale",    "vn");
            params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
            params.put("vnp_IpAddr",    vnPayConfig.getIpAddress(httpRequest));

            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
            SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss");
            fmt.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
            params.put("vnp_CreateDate", fmt.format(cal.getTime()));
            cal.add(Calendar.MINUTE, 15);
            params.put("vnp_ExpireDate", fmt.format(cal.getTime()));

            // Build sorted query & hashData
            List<String> keys = new ArrayList<>(params.keySet());
            Collections.sort(keys);
            StringBuilder hashData = new StringBuilder();
            StringBuilder query    = new StringBuilder();

            for (String k : keys) {
                String v = params.get(k);
                if (v != null && !v.isEmpty()) {
                    if (hashData.length() > 0) { hashData.append('&'); query.append('&'); }
                    hashData.append(k).append('=').append(URLEncoder.encode(v, StandardCharsets.UTF_8));
                    query.append(URLEncoder.encode(k, StandardCharsets.UTF_8))
                         .append('=').append(URLEncoder.encode(v, StandardCharsets.UTF_8));
                }
            }

            String secureHash = vnPayConfig.hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
            String paymentUrl = vnPayConfig.getPayUrl() + "?" + query + "&vnp_SecureHash=" + secureHash;

            log.info("VNPay URL created - enrollmentId={}, txnRef={}", enrollmentId, vnpTxnRef);
            return VNPayResponseDTO.success(paymentUrl);

        } catch (Exception e) {
            log.error("Error creating VNPay URL for enrollment {}: {}", enrollmentId, e.getMessage(), e);
            return VNPayResponseDTO.error("99", "Lỗi tạo URL thanh toán: " + e.getMessage());
        }
    }

    @Override
    public VNPayResponseDTO processPaymentReturn(Map<String, String> params) {
        try {
            String receivedHash = params.get("vnp_SecureHash");

            // Remove hash fields before re-computing
            Map<String, String> fields = new HashMap<>(params);
            fields.remove("vnp_SecureHashType");
            fields.remove("vnp_SecureHash");

            String computedHash = vnPayConfig.hashAllFields(fields);

            VNPayResponseDTO response = VNPayResponseDTO.builder()
                    .txnRef(params.get("vnp_TxnRef"))
                    .transactionNo(params.get("vnp_TransactionNo"))
                    .responseCode(params.get("vnp_ResponseCode"))
                    .transactionStatus(params.get("vnp_TransactionStatus"))
                    .bankCode(params.get("vnp_BankCode"))
                    .bankTranNo(params.get("vnp_BankTranNo"))
                    .payDate(params.get("vnp_PayDate"))
                    .orderInfo(params.get("vnp_OrderInfo"))
                    .build();

            String amountStr = params.get("vnp_Amount");
            if (amountStr != null) {
                response.setAmount(new BigDecimal(amountStr).divide(BigDecimal.valueOf(100)));
            }

            if (computedHash.equals(receivedHash)) {
                String rc = params.get("vnp_ResponseCode");
                if ("00".equals(rc)) {
                    response.setCode("00");
                    response.setMessage("Giao dịch thành công");
                } else {
                    response.setCode(rc);
                    response.setMessage(getResponseMessage(rc));
                }
            } else {
                response.setCode("97");
                response.setMessage("Chữ ký không hợp lệ — kết quả thanh toán không thể xác nhận.");
            }

            log.info("VNPay return processed: txnRef={}, code={}", response.getTxnRef(), response.getCode());
            return response;

        } catch (Exception e) {
            log.error("Error processing VNPay return: {}", e.getMessage(), e);
            return VNPayResponseDTO.error("99", "Lỗi xử lý kết quả thanh toán");
        }
    }

    private String getResponseMessage(String code) {
        return switch (code) {
            case "07" -> "Giao dịch bị nghi ngờ (lừa đảo)";
            case "09" -> "Thẻ/TK chưa đăng ký InternetBanking";
            case "10" -> "Xác thực sai quá 3 lần";
            case "11" -> "Đã hết hạn thanh toán";
            case "12" -> "Thẻ/TK bị khóa";
            case "13" -> "Nhập sai OTP";
            case "24" -> "Khách hàng hủy giao dịch";
            case "51" -> "Tài khoản không đủ số dư";
            case "65" -> "Đã vượt hạn mức giao dịch trong ngày";
            case "75" -> "Ngân hàng đang bảo trì";
            case "79" -> "Nhập sai mật khẩu quá số lần cho phép";
            default   -> "Lỗi không xác định (code=" + code + ")";
        };
    }
}
