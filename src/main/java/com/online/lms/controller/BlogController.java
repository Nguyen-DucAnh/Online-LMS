package com.online.lms.controller;

import com.online.lms.dto.post.CommentCreateDTO;
import com.online.lms.entity.Post;
import com.online.lms.entity.PostComment;
import com.online.lms.service.BlogCategoryService;
import com.online.lms.service.PostService;
import com.online.lms.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/blogs")
public class BlogController {

    private final PostService postService;
    private final BlogCategoryService blogCategoryService;
    private final UserService userService;

    @GetMapping
    public String list(Model model,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Long categoryId,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "6") int size) {

        model.addAttribute("posts", postService.searchPublished(keyword, categoryId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
        model.addAttribute("categories", blogCategoryService.findAllActive());
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        return "blog/list";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        Post post = postService.findPublishedOrThrow(id);
        List<PostComment> comments = postService.getRootComments(id);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            model.addAttribute("currentUserId", userService.getCurrentUser().getId());
        }

        model.addAttribute("post", post);
        model.addAttribute("comments", comments);
        model.addAttribute("commentForm", new CommentCreateDTO());
        model.addAttribute("categories", blogCategoryService.findAllActive());
        return "blog/details";
    }

    @PostMapping("/{id}/comments")
    @PreAuthorize("isAuthenticated()")
    public String addComment(@PathVariable Long id,
                             @Valid @ModelAttribute("commentForm") CommentCreateDTO dto,
                             BindingResult result,
                             RedirectAttributes ra,
                             Model model) {
        if (result.hasErrors()) {
            Post post = postService.findPublishedOrThrow(id);
            model.addAttribute("post", post);
            model.addAttribute("comments", postService.getRootComments(id));
            model.addAttribute("categories", blogCategoryService.findAllActive());
     return "blog/details";
        }

        postService.addComment(id, dto);
        ra.addFlashAttribute("successMessage", "Đã gửi bình luận!");
        return "redirect:/blogs/" + id;
    }

    @PostMapping("/comments/{commentId}/delete")
    @PreAuthorize("isAuthenticated()")
    public String deleteComment(@PathVariable Long commentId,
                                @RequestParam("postId") Long postId,
                                RedirectAttributes ra) {
        postService.deleteComment(commentId);
        ra.addFlashAttribute("successMessage", "Đã xóa bình luận!");
        return "redirect:/blogs/" + postId;
    }
}
