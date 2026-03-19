package com.online.lms.repository;

import com.online.lms.entity.Post;
import com.online.lms.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("""
            SELECT p FROM Post p
            LEFT JOIN p.category cat
            LEFT JOIN p.author au
            WHERE (:keyword IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:categoryId IS NULL OR cat.id = :categoryId)
              AND (:status IS NULL OR p.status = :status)
              AND (:authorId IS NULL OR au.id = :authorId)
            """)
    Page<Post> searchAdmin(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("status") PostStatus status,
            @Param("authorId") Long authorId,
            Pageable pageable);

    @Query("""
            SELECT p FROM Post p
            LEFT JOIN p.category cat
            LEFT JOIN p.author au
            WHERE p.status = 'PUBLISHED'
              AND (:keyword IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:categoryId IS NULL OR cat.id = :categoryId)
            """)
    Page<Post> searchPublished(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            Pageable pageable);

    @Query("""
            SELECT p FROM Post p
            LEFT JOIN FETCH p.category
            LEFT JOIN FETCH p.author
            WHERE p.id = :id
            """)
    Optional<Post> findByIdWithCategoryAuthor(@Param("id") Long id);
}
