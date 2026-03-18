package com.online.lms.dto.enrollment;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO cho trang My Courses.
 *
 * Fields khớp chính xác với my-courses.html:
 *   course.enrollmentId    → link Start Learning
 *   course.courseTitle     → tên khóa học
 *   course.courseThumbnail → ảnh thumbnail
 *   course.enrollDate      → "Enrolled on: ..."
 *   course.progress        → BigDecimal hiển thị "0.00%"
 *   course.progressInt     → int dùng cho width của progress bar CSS
 *   course.completed       → boolean: true → "Completed", false → "In Progress"
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyCourseDTO {

    private Long enrollmentId;
    private Long courseId;

    private String courseTitle;
    private String courseThumbnail;
    private String categoryName;
    private String instructorName;

    private LocalDateTime enrollDate;

    // progress dạng BigDecimal: 0.00 → 100.00
    // Template dùng: #numbers.formatDecimal(course.progress, 1, 2) + '%'
    private BigDecimal progress;

    // Template dùng: th:style="'width:' + ${course.progressInt} + '%'"
    // Cần là int (không phải BigDecimal) để tránh render "45.50%"
    private int progressInt;

    // Template dùng: th:if="${course.completed}"
    private boolean completed;
}