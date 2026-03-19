package com.online.lms.repository;

import com.online.lms.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    @Query("""
            SELECT DISTINCT c FROM PostComment c
            LEFT JOIN FETCH c.author
            LEFT JOIN FETCH c.replies r
            LEFT JOIN FETCH r.author
            WHERE c.post.id = :postId
              AND c.parent IS NULL
            ORDER BY c.createdAt ASC
            """)
    List<PostComment> findRootWithReplies(@Param("postId") Long postId);
}
