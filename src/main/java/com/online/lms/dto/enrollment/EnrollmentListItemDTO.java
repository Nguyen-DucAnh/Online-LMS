package com.online.lms.dto.enrollment;

import com.online.lms.enums.EnrollmentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentListItemDTO {

    private Long id;

    //Course
    private Long courseId;
    private String courseTitle;
    private BigDecimal fee;   // hiển thị "Fee($): 68.00" dưới course title

    //User Info
    private Long userId;
    private String fullName;
    private String email;

    //Enrolled At
    private LocalDateTime enrollDate;

    //Status badge
    private EnrollmentStatus status;

    //Progress
    private BigDecimal progress;
}
