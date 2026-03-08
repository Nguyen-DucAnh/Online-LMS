package com.online.lms.dto.course;

import com.online.lms.entity.enums.CourseLevel;
import com.online.lms.entity.enums.CourseStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseFormDTO {

    private Integer id;

    @NotBlank(message = "Tên khóa học không được để trống")
    @Size(max = 255, message = "Tên không quá 255 ký tự")
    private String title;

    private String description;

    private String thumbnail;

    @NotNull(message = "Vui lòng chọn danh mục")
    private Integer categoryId;

    private Integer instructorId;

    private CourseLevel level;

    @DecimalMin(value = "0.0", message = "Giá niêm yết không được âm")
    private BigDecimal listedPrice;

    @DecimalMin(value = "0.0", message = "Giá sale không được âm")
    private BigDecimal salePrice;

    private Integer duration;

    private CourseStatus status;

    private Boolean featured;
}
