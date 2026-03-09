package com.online.lms.dto.request.user;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserRequestDTO {
    private Long id;

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại phải có 10-11 chữ số")
    private String phone;

    private String address;
    private String role;
    private String status;
}