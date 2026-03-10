package com.online.lms.dto.course;


import com.online.lms.enums.CourseLevel;
import com.online.lms.enums.CourseStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseListItemDTO {

    private Long id;
    private String title;
    private String description;
    private String thumbnail;
    private String categoryName;
    private Long categoryId;
    private String instructorName;
    private BigDecimal listedPrice;
    private BigDecimal salePrice;
    private Integer duration;
    private CourseStatus status;
    private CourseLevel level;
    private Boolean featured;
    private int chapterCount;
    private LocalDateTime createdAt;
}
