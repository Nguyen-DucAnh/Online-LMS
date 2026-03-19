package com.online.lms.dto.post;

import com.online.lms.enums.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
