package com.online.lms.dto.chapter;


import com.online.lms.enums.LessonType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonDTO {

    private Long id;

    @NotBlank(message = "Tên bài học không được để trống")
    @Size(max = 255, message = "Tên bài học không quá 255 ký tự")
    private String title;

    private String content;

    @NotNull(message = "Vui lòng chọn loại bài học")
    private LessonType type;

    @Min(value = 0, message = "Thời lượng không được âm")
    private Integer duration;

    private Boolean status;

    private Integer orderIndex;

    private Long chapterId;

    /** Path to uploaded video/PDF file */
    private String contentFilePath;

    /** Preview lessons can be watched without enrollment */
    private Boolean previewEnabled;
}
