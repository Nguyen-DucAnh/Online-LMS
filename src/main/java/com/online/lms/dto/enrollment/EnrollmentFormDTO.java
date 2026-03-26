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

    private Long courseId;

    private String courseTitle;

    private BigDecimal fee;

    private String usernameOrEmail;

    private String fullName;

    private String enrollNote;

    private EnrollmentStatus status;

    private String rejectedNotes;

    private BigDecimal progress;

    private LocalDateTime completedAt;

    private LocalDateTime updatedAt;
}