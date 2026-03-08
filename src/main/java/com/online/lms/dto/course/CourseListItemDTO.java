package com.online.lms.dto.course;

import com.online.lms.entity.enums.CourseLevel;
import com.online.lms.entity.enums.CourseStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseListItemDTO {

    private int id;
    private String title;
    private String thumbnail;
    private String categoryName;
    private String instructorName;
    private BigDecimal listedPrice;
    private CourseStatus status;
    private CourseLevel level;
    private Boolean featured;
    private int chapterCount;
    private LocalDateTime createdAt;
}
