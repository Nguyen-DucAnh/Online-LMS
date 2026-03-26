package com.online.lms.dto.enrollment;

import com.online.lms.enums.PaymentMethod;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class EnrollmentRequestDTO {

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100)
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 150)
    private String email;

    @Size(max = 20)
    private String phone;

    @Size(max = 500)
    private String enrollNote;

    private PaymentMethod paymentMethod = PaymentMethod.BANK_TRANSFER;
}
