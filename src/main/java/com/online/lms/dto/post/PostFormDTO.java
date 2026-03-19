package com.online.lms.dto.post;

import com.online.lms.enums.PostStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostFormDTO {

    private Long id;

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 255, message = "Tiêu đề không quá 255 ký tự")
    private String title;

    @Size(max = 500, message = "Mô tả ngắn không quá 500 ký tự")
    private String briefInfo;

    @NotBlank(message = "Nội dung không được để trống")
    private String content;

    @Size(max = 500, message = "Thumbnail không quá 500 ký tự")
    private String thumbnail;

    @NotNull(message = "Vui lòng chọn danh mục")
    private Long categoryId;

    private PostStatus status;
}
