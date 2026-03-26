package com.online.lms.entity;

import com.online.lms.enums.EnrollmentStatus;
import com.online.lms.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "enrollments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "enroll_note", length = 500)
    private String enrollNote;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 30)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private EnrollmentStatus status = EnrollmentStatus.PENDING;

    @Column(name = "rejected_notes", length = 500)
    private String rejectedNotes;

    @Column(name = "fee", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal fee = BigDecimal.ZERO;


    @Column(name = "progress", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal progress = BigDecimal.ZERO;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;


    @Column(name = "enroll_date", nullable = false)
    private LocalDateTime enrollDate;

    @PrePersist
    protected void onEnrollPrePersist() {
        if (enrollDate == null) {
            enrollDate = LocalDateTime.now();
        }
    }

    @Column(name = "vnpay_transaction_no", length = 50)
    private String vnpayTransactionNo;
}