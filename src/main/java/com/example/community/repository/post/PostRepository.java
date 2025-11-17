package com.example.community.repository.post;

import com.example.community.domain.Post;
import com.example.community.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query(value = "SELECT p FROM Post p JOIN FETCH p.user WHERE p.id = :postId")
    Optional<Post> findByIdWithUser(Long postId);

    @Query(value = "SELECT p FROM Post p JOIN FETCH p.user")
    Page<Post> findAllWithUser(Pageable pageable);

    @Query(value = "SELECT p FROM Post p JOIN FETCH p.user WHERE p.user = :user")
    Page<Post> findAllByUser(User user , Pageable pageable);

    @Query(value = "SELECT p FROM Post p JOIN FETCH p.user WHERE p.user = :user")
    List<Post> findAllByUser(User user);

    @Query(value = "SELECT p FROM Post p JOIN FETCH p.user")
    Slice<Post> findAllWithUserSlice(Pageable pageable);

    @Query(value = "SELECT count(p) FROM Post p WHERE p.user.id = :userId")
    long countByUserId(Long userId);
}
