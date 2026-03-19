package com.online.lms.dto.course;

import com.online.lms.enums.CourseLevel;
import com.online.lms.enums.CourseStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseFormDTO {

    private Long id;

    @NotBlank(message = "Tên khóa học không được để trống")
    @Size(max = 255, message = "Tên không quá 255 ký tự")
    private String title;

    @Size(max = 5000, message = "Mô tả không quá 5000 ký tự")
    private String description;

    private String thumbnail;

    private MultipartFile thumbnailFile;

    @NotNull(message = "Vui lòng chọn danh mục")
    private Long categoryId;

    private Long instructorId;

    private CourseLevel level;

    @NotNull(message = "Vui lòng nhập giá niêm yết")
    @DecimalMin(value = "0.0", message = "Giá niêm yết không được âm")
    private BigDecimal listedPrice;

    @DecimalMin(value = "0.0", message = "Giá sale không được âm")
    private BigDecimal salePrice;

    @NotNull(message = "Vui lòng nhập thời lượng khóa học")
    @Min(value = 1, message = "Thời lượng phải ít nhất 1 phút")
    private Integer duration;

    private CourseStatus status;

    private Boolean featured;
}
