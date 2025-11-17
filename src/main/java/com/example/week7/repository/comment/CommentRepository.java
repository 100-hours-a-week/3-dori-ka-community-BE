package com.example.week7.repository.comment;

import com.example.week7.domain.Comment;
import com.example.week7.domain.User;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query(value = "SELECT c FROM Comment c JOIN FETCH c.user WHERE c.id = :commentId")
    Optional<Comment> findByIdWithUser(Long commentId);

    @Query(value = "SELECT c FROM Comment  c JOIN FETCH c.user WHERE c.post.id = :postId")
    Page<Comment> findAllByPostIdWithUser(Long postId, Pageable pageable);

    @Query("SELECT c FROM Comment c JOIN FETCH c.user JOIN FETCH c.post WHERE c.user = :user")
    Page<Comment> findAllByUser(User user, Pageable pageable);

    List<Comment> findAllByUser(User user);

    @Query(value = "SELECT count(c) from Comment c WHERE c.post.id = :postId")
    long countByPostId(Long postId);

    @Query(value = "SELECT count(c) from Comment c WHERE c.user.id = :userId")
    long countByUserId(Long userId);
}
