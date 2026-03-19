package com.online.lms.utils;

import com.online.lms.dto.post.PostFormDTO;
import com.online.lms.dto.post.PostListItemDTO;
import com.online.lms.entity.Post;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostMapper {

    public static PostListItemDTO toListItemDTO(Post p) {
        return PostListItemDTO.builder()
                .id(p.getId())
                .title(p.getTitle())
                .briefInfo(p.getBriefInfo())
                .thumbnail(p.getThumbnail())
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : "—")
                .authorName(p.getAuthor() != null ? p.getAuthor().getFullName() : "—")
                .status(p.getStatus())
                .createdAt(p.getCreatedAt())
                .build();
    }

    public static PostFormDTO toFormDTO(Post p) {
        return PostFormDTO.builder()
                .id(p.getId())
                .title(p.getTitle())
                .briefInfo(p.getBriefInfo())
                .content(p.getContent())
                .thumbnail(p.getThumbnail())
                .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
                .status(p.getStatus())
                .build();
    }
}
