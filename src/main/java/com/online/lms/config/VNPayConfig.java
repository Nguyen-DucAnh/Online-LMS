package com.online.lms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Configuration
public class VNPayConfig {

    @Value("${vnpay.tmn-code}")
    private String tmnCode;

    @Value("${vnpay.hash-secret}")
    private String hashSecret;

    @Value("${vnpay.pay-url}")
    private String payUrl;

    @Value("${vnpay.return-url}")
    private String returnUrl;

    @Value("${vnpay.api-url}")
    private String apiUrl;

    @Value("${vnpay.version}")
    private String version;

    @Value("${vnpay.command}")
    private String command;

    @Value("${vnpay.order-type}")
    private String orderType;

    public String getTmnCode()  { return tmnCode;  }
    public String getHashSecret() { return hashSecret; }
    public String getPayUrl()   { return payUrl;   }
    public String getReturnUrl(){ return returnUrl; }
    public String getApiUrl()   { return apiUrl;   }
    public String getVersion()  { return version;  }
    public String getCommand()  { return command;  }
    public String getOrderType(){ return orderType; }

    /** HMAC-SHA512 signature, same as VNPay spec. */
    public String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("HMAC-SHA512 failed", e);
        }
    }

    /** Build hash string from all non-empty params (sorted by key). */
    public String hashAllFields(Map<String, String> fields) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder sb = new StringBuilder();
        for (String name : fieldNames) {
            String value = fields.get(name);
            if (value != null && !value.isEmpty()) {
                if (sb.length() > 0) sb.append("&");
                sb.append(name).append("=")
                  .append(URLEncoder.encode(value, StandardCharsets.UTF_8));
            }
        }
        return hmacSHA512(hashSecret, sb.toString());
    }

    /** Extract real IP from proxy headers. */
    public String getIpAddress(jakarta.servlet.http.HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip))
            ip = request.getHeader("Proxy-Client-IP");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip))
            ip = request.getHeader("WL-Proxy-Client-IP");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip))
            ip = request.getRemoteAddr();
        if (ip != null && ip.contains(","))
            ip = ip.split(",")[0].trim();
        return ip != null ? ip : "127.0.0.1";
    }
}
