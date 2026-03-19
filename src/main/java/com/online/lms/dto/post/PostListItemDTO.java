package com.online.lms.dto.post;

import com.online.lms.enums.PostStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostListItemDTO {

    private Long id;
    private String title;
    private String briefInfo;
    private String thumbnail;
    private String categoryName;
    private String authorName;
    private PostStatus status;
    private LocalDateTime createdAt;
}
