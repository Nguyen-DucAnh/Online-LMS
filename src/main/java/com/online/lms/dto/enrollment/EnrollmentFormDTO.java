package com.online.lms.dto.enrollment;

import com.online.lms.enums.EnrollmentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnrollmentFormDTO {

    private Long id;

    // Không dùng @NotNull ở đây vì khi edit, courseId được truyền qua hidden field
    // Validation thực sự xảy ra trong service khi tạo mới
    private Long courseId;

    private String courseTitle;

    private BigDecimal fee;

    //Dùng để tìm user (nhập username hoặc email)
    private String usernameOrEmail;

    private String fullName;

    private String enrollNote;  //"Enroll Reason" trong form

    private EnrollmentStatus status;

    private String rejectedNotes;

    private BigDecimal progress;

    private LocalDateTime completedAt;

    private LocalDateTime updatedAt;  // "Last Updated" - chỉ hiển thị
}