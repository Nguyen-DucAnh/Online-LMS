package com.online.lms.service.impl;

import com.online.lms.dto.post.CommentCreateDTO;
import com.online.lms.dto.post.PostFormDTO;
import com.online.lms.dto.post.PostListItemDTO;
import com.online.lms.entity.BlogCategory;
import com.online.lms.entity.Post;
import com.online.lms.entity.PostComment;
import com.online.lms.entity.User;
import com.online.lms.enums.PostStatus;
import com.online.lms.exceptions.ResourceNotFoundException;
import com.online.lms.repository.BlogCategoryRepository;
import com.online.lms.repository.PostCommentRepository;
import com.online.lms.repository.PostRepository;
import com.online.lms.service.PostService;
import com.online.lms.service.UserService;
import com.online.lms.utils.PostMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final BlogCategoryRepository blogCategoryRepository;
    private final PostCommentRepository postCommentRepository;
    private final UserService userService;

    @Override
    public Page<PostListItemDTO> searchAdmin(String keyword, Long categoryId, PostStatus status, Pageable pageable) {
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return postRepository.searchAdmin(kw, categoryId, status, null, pageable)
                .map(PostMapper::toListItemDTO);
    }

    @Override
    public Page<PostListItemDTO> searchAdminOwned(String keyword, Long categoryId, PostStatus status, Pageable pageable) {
        User u = userService.getCurrentUser();
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return postRepository.searchAdmin(kw, categoryId, status, u.getId(), pageable)
                .map(PostMapper::toListItemDTO);
    }

    @Override
    public Page<PostListItemDTO> searchPublished(String keyword, Long categoryId, Pageable pageable) {
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return postRepository.searchPublished(kw, categoryId, pageable)
                .map(PostMapper::toListItemDTO);
    }

    @Override
    public PostFormDTO findFormById(Long id) {
        return PostMapper.toFormDTO(getPostOrThrow(id));
    }

    @Override
    public Post findPublishedOrThrow(Long id) {
        Post p = getPostOrThrow(id);
        if (p.getStatus() != PostStatus.PUBLISHED) {
            throw new ResourceNotFoundException("Post not published id=" + id);
        }
        return p;
    }

    @Override
    public Post findById(Long id) {
        return getPostOrThrow(id);
    }

    @Override
    @Transactional
    public void create(PostFormDTO dto) {
        User u = userService.getCurrentUser();
        Post post = Post.builder()
                .title(dto.getTitle())
                .briefInfo(dto.getBriefInfo())
                .content(dto.getContent())
                .thumbnail(dto.getThumbnail())
                .status(dto.getStatus() != null ? dto.getStatus() : PostStatus.DRAFT)
                .category(getCategoryOrThrow(dto.getCategoryId()))
                .author(u)
                .build();
        postRepository.save(post);
        log.info("Post created: id={} title={}", post.getId(), post.getTitle());
    }

    @Override
    @Transactional
    public void update(Long id, PostFormDTO dto) {
        Post post = getPostOrThrow(id);
        enforceOwnerOrPrivileged(post);

        post.setTitle(dto.getTitle());
        post.setBriefInfo(dto.getBriefInfo());
        post.setContent(dto.getContent());
        post.setThumbnail(dto.getThumbnail());
        post.setCategory(getCategoryOrThrow(dto.getCategoryId()));

        User u = userService.getCurrentUser();
        if (u.getRole().name().equals("MARKETING")) {
            // Marketer cannot change status
        } else if (dto.getStatus() != null) {
            post.setStatus(dto.getStatus());
        }

        postRepository.save(post);
        log.info("Post updated: id={}", id);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, PostStatus status) {
        Post post = getPostOrThrow(id);
        User u = userService.getCurrentUser();
        if (u.getRole().name().equals("MARKETING")) {
            throw new IllegalStateException("Marketer is not allowed to change post status");
        }
        post.setStatus(status);
        postRepository.save(post);
        log.info("Post status updated: id={} status={}", id, status);
    }

    @Override
    public List<PostComment> getRootComments(Long postId) {
        return postCommentRepository.findRootWithReplies(postId);
    }

    @Override
    @Transactional
    public void addComment(Long postId, CommentCreateDTO dto) {
        User u = userService.getCurrentUser();
        Post post = findPublishedOrThrow(postId);

        PostComment parent = null;
        if (dto.getParentId() != null) {
            parent = postCommentRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Comment not found id=" + dto.getParentId()));
            if (!parent.getPost().getId().equals(post.getId())) {
                throw new IllegalArgumentException("Parent comment does not belong to this post");
            }
            if (parent.getParent() != null) {
                throw new IllegalArgumentException("Only one reply level is supported");
            }
        }

        PostComment c = PostComment.builder()
                .post(post)
                .author(u)
                .content(dto.getContent())
                .parent(parent)
                .build();
        postCommentRepository.save(c);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        User u = userService.getCurrentUser();
        PostComment c = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found id=" + commentId));

        boolean isOwner = c.getAuthor() != null && c.getAuthor().getId() != null
                && c.getAuthor().getId().equals(u.getId());
        boolean isPrivileged = u.getRole().name().equals("ADMIN") || u.getRole().name().equals("MANAGER");

        if (!isOwner && !isPrivileged) {
            throw new IllegalStateException("Not allowed to delete this comment");
        }

        postCommentRepository.delete(c);
    }

    private Post getPostOrThrow(Long id) {
        return postRepository.findByIdWithCategoryAuthor(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài đăng id=" + id));
    }

    private BlogCategory getCategoryOrThrow(Long id) {
        return blogCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục blog id=" + id));
    }

    private void enforceOwnerOrPrivileged(Post post) {
        User u = userService.getCurrentUser();
        boolean privileged = u.getRole().name().equals("ADMIN") || u.getRole().name().equals("MANAGER");
        if (privileged) return;

        if (post.getAuthor() == null || post.getAuthor().getId() == null || !post.getAuthor().getId().equals(u.getId())) {
            throw new IllegalStateException("Not allowed to access this post");
        }
    }
}
