package com.online.lms.service;

import com.online.lms.dto.post.CommentCreateDTO;
import com.online.lms.dto.post.PostFormDTO;
import com.online.lms.dto.post.PostListItemDTO;
import com.online.lms.entity.Post;
import com.online.lms.entity.PostComment;
import com.online.lms.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostService {

    Page<PostListItemDTO> searchAdmin(String keyword, Long categoryId, PostStatus status, Pageable pageable);

    Page<PostListItemDTO> searchAdminOwned(String keyword, Long categoryId, PostStatus status, Pageable pageable);

    Page<PostListItemDTO> searchPublished(String keyword, Long categoryId, Pageable pageable);

    PostFormDTO findFormById(Long id);

    Post findPublishedOrThrow(Long id);

    Post findById(Long id);

    void create(PostFormDTO dto);

    void update(Long id, PostFormDTO dto);

    void updateStatus(Long id, PostStatus status);

    List<PostComment> getRootComments(Long postId);

    void addComment(Long postId, CommentCreateDTO dto);

    void deleteComment(Long commentId);
}
