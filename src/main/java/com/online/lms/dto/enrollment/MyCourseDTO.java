package com.online.lms.dto.enrollment;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


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

    private BigDecimal progress;


    private int progressInt;


    private boolean completed;
}