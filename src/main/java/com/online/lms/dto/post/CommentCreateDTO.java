package com.online.lms.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentCreateDTO {

    @NotBlank(message = "Nội dung bình luận không được để trống")
    @Size(max = 2000, message = "Nội dung bình luận không quá 2000 ký tự")
    private String content;

    private Long parentId;
}
