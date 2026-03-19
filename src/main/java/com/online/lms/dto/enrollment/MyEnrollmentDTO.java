package com.online.lms.dto.enrollment;

import com.online.lms.enums.EnrollmentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO cho trang My Enrollments.
 * Columns trong UI: Id | Course | User Info | Enrolled At | Fee($) | Status | Actions
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyEnrollmentDTO {

    private Long id;

    // Course
    private Long courseId;
    private String courseTitle;
    private String courseThumbnail;
    private String categoryName;

    // User Info (hiển thị fullName + email trong 1 ô)
    private String fullName;
    private String email;

    // Enrolled At
    private LocalDateTime enrollDate;

    // Fee($)
    private BigDecimal fee;

    // Status
    private EnrollmentStatus status;

    // Dùng để hiện lý do khi REJECTED
    private String rejectedNotes;

    // Dùng để hiện progress khi APPROVED
    private BigDecimal progress;
    private LocalDateTime completedAt;

    /**
     * Helper: trả về label action theo status — dùng trong Thymeleaf.
     * Pending/Rejected → "Update" | Approved → "Access" | Cancelled → "View"
     */
    public String getActionLabel() {
        if (status == null) return "View";
        return switch (status) {
            case PENDING, REJECTED -> "Update";
            case APPROVED          -> "Access";
            case CANCELLED         -> "View";
        };
    }

    /**
     * Helper: CSS class cho badge status — dùng trong Thymeleaf.
     */
    public String getStatusBadgeClass() {
        if (status == null) return "badge bg-secondary";
        return switch (status) {
            case PENDING   -> "badge bg-warning text-dark";
            case APPROVED  -> "badge bg-success";
            case REJECTED  -> "badge bg-danger";
            case CANCELLED -> "badge bg-secondary";
        };
    }
}