package com.online.lms.entity;

import com.online.lms.enums.EnrollmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Enrollment entity — map bảng enrollments.
 *
 * Theo đúng pattern của team:
 * - Extends BaseEntity (có @PrePersist / @PreUpdate tự set CreatedAt/UpdatedAt)
 * - @Builder, @Getter, @Setter, @NoArgsConstructor, @AllArgsConstructor
 * - Lazy fetch để tránh N+1
 * - Column name khớp 100% với DB schema
 */
@Entity
@Table(name = "enrollments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment extends BaseEntity {

    // User đăng ký
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Khóa học đăng ký
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    // Snapshot thông tin tại thời điểm đăng ký
    // (user có thể đổi profile sau — enrollment giữ data gốc)
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "enroll_note", length = 500)
    private String enrollNote;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private EnrollmentStatus status = EnrollmentStatus.PENDING;

    // Lý do từ chối — chỉ có giá trị khi status = REJECTED
    @Column(name = "rejected_notes", length = 500)
    private String rejectedNotes;

    @Column(name = "fee", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal fee = BigDecimal.ZERO;

    // Tiến độ học: 0.00 → 100.00 (%)
    @Column(name = "progress", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal progress = BigDecimal.ZERO;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // Thời điểm submit form đăng ký
    @Column(name = "enroll_date", nullable = false)
    private LocalDateTime enrollDate;

    // Set enrollDate trước khi persist (BaseEntity đã handle CreatedAt/UpdatedAt)
    @PrePersist
    protected void onEnrollPrePersist() {
        if (enrollDate == null) {
            enrollDate = LocalDateTime.now();
        }
    }
}