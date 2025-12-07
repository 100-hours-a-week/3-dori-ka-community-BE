package com.example.community.service.post.like;

import com.example.community.common.exception.custom.BadRequestException;
import com.example.community.common.exception.custom.DuplicatedException;
import com.example.community.common.exception.custom.ResourceNotFoundException;
import com.example.community.domain.Post;
import com.example.community.domain.PostLike;
import com.example.community.domain.User;
import com.example.community.dto.response.post.PostLikeResponse;
import com.example.community.repository.post.PostLikeRepository;
import com.example.community.repository.post.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostLikeServiceTest {

    private static final long POST_ID = 1L;
    private static final long USER_ID = 10L;

    @Mock
    private PostRepository postRepository;
    @Mock
    private PostLikeRepository postLikeRepository;

    @InjectMocks
    private PostLikeServiceImpl postLikeService;

    @Test
    @DisplayName("게시글 좋아요 추가 - 성공")
    void add_post_like_success() {
        User user = createUser(USER_ID);
        Post post = createPost(POST_ID, user);

        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));
        when(postLikeRepository.existsByPostIdAndUserId(POST_ID, USER_ID)).thenReturn(false);
        when(postLikeRepository.save(any(PostLike.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(postLikeRepository.countByPostId(POST_ID)).thenReturn(1L);

        PostLikeResponse response = postLikeService.addLike(POST_ID, user);

        assertThat(response.getPostId()).isEqualTo(POST_ID);
        assertThat(response.getLiked()).isTrue();
        assertThat(response.getLikeCount()).isEqualTo(1L);

        verify(postRepository).findById(POST_ID);
        verify(postLikeRepository).existsByPostIdAndUserId(POST_ID, USER_ID);
        verify(postLikeRepository).save(any(PostLike.class));
        verify(postLikeRepository).countByPostId(POST_ID);
    }

    @Test
    @DisplayName("게시글 좋아요 추가 - 실패(존재하지 않는 게시글)")
    void add_post_like_fail_no_post() {
        User user = createUser(USER_ID);
        Long wrongId = 100L;

        when(postRepository.findById(wrongId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postLikeService.addLike(wrongId, user))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(postRepository).findById(wrongId);
        verify(postLikeRepository, never()).existsByPostIdAndUserId(wrongId, USER_ID);
        verify(postLikeRepository, never()).save(any(PostLike.class));
        verify(postLikeRepository, never()).countByPostId(wrongId);
    }

    @Test
    @DisplayName("게시글 좋아요 추가 - 실패(중복 좋아요)")
    void add_post_like_fail_duplicate() {
        User user = createUser(USER_ID);
        Post post = createPost(POST_ID, user);

        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));
        when(postLikeRepository.existsByPostIdAndUserId(POST_ID, USER_ID))
                .thenReturn(true);

        assertThatThrownBy(() -> postLikeService.addLike(POST_ID, user))
                .isInstanceOf(DuplicatedException.class);

        verify(postRepository).findById(POST_ID);
        verify(postLikeRepository).existsByPostIdAndUserId(POST_ID, USER_ID);
        verify(postLikeRepository, never()).save(any(PostLike.class));
        verify(postLikeRepository, never()).countByPostId(POST_ID);
    }

    @Test
    @DisplayName("게시글 좋아요 취소 - 성공")
    void remove_like_success() {
        User user = createUser(USER_ID);
        Post post = createPost(POST_ID, user);
        PostLike postLike = createPostLike(post, user);

        when(postLikeRepository.findByPostIdAndUserId(POST_ID, USER_ID))
                .thenReturn(Optional.of(postLike));
        when(postLikeRepository.countByPostId(POST_ID)).thenReturn(0L);

        PostLikeResponse response = postLikeService.removeLike(POST_ID, user);

        assertThat(response.getPostId()).isEqualTo(POST_ID);
        assertThat(response.getLikeCount()).isEqualTo(0L);
        assertThat(response.getLiked()).isFalse();

        verify(postLikeRepository).findByPostIdAndUserId(POST_ID, USER_ID);
        verify(postLikeRepository).delete(postLike);
        verify(postLikeRepository).countByPostId(POST_ID);
    }

    @Test
    @DisplayName("게시글 좋아요 취소 - 실패(좋아요 없음)")
    void remove_like_fail() {
        User user = createUser(USER_ID);

        when(postLikeRepository.findByPostIdAndUserId(POST_ID, USER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> postLikeService.removeLike(POST_ID, user))
                .isInstanceOf(BadRequestException.class);

        verify(postLikeRepository).findByPostIdAndUserId(POST_ID, USER_ID);
        verify(postLikeRepository, never()).delete(any(PostLike.class));
        verify(postLikeRepository, never()).countByPostId(POST_ID);
    }

    @Test
    @DisplayName("게시글 좋아요 조회 - 성공")
    void get_like_count_success() {
        User user = createUser(USER_ID);

        when(postLikeRepository.existsByPostIdAndUserId(POST_ID, USER_ID))
                .thenReturn(true);
        when(postLikeRepository.countByPostId(POST_ID))
                .thenReturn(5L);

        PostLikeResponse response = postLikeService.getLikeCount(POST_ID, user);

        assertThat(response.getPostId()).isEqualTo(POST_ID);
        assertThat(response.getLikeCount()).isEqualTo(5L);
        assertThat(response.getLiked()).isTrue();

        verify(postLikeRepository).existsByPostIdAndUserId(POST_ID, USER_ID);
        verify(postLikeRepository).countByPostId(POST_ID);
    }

    private User createUser(Long id) {
        User user = User.builder()
                .email("test@test.com")
                .password("password")
                .nickname("test")
                .profileImage("profile.jpg")
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Post createPost(Long id, User user) {
        Post post = Post.builder()
                .title("title")
                .content("content")
                .user(user)
                .build();
        ReflectionTestUtils.setField(post, "id", id);
        return post;
    }

    private PostLike createPostLike(Post post, User user) {
        PostLike like = PostLike.builder()
                .post(post)
                .user(user)
                .build();
        ReflectionTestUtils.setField(like, "id", 100L);
        return like;
    }
}