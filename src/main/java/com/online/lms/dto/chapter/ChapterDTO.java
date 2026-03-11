package com.online.lms.dto.chapter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChapterDTO {

    private Long id;

    @NotBlank(message = "Tên chương không được để trống")
    @Size(max = 255, message = "Tên chương không quá 255 ký tự")
    private String title;

    private String description;

    private Long orderIndex;

    private Long courseId;

    @Builder.Default
    private List<LessonDTO> lessons = new ArrayList<>();
}
