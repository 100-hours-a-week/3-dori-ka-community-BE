package com.example.community.service.comment;

import com.example.community.common.exception.custom.ResourceNotFoundException;
import com.example.community.common.util.AuthValidator;
import com.example.community.domain.Comment;
import com.example.community.domain.Post;
import com.example.community.domain.User;
import com.example.community.dto.request.comment.CommentRequestDto;
import com.example.community.dto.response.comment.CommentResponse;
import com.example.community.repository.comment.CommentRepository;
import com.example.community.repository.post.PostRepository;
import com.example.community.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private AuthValidator authValidator;
    @Mock
    private UserRepository userRepository;


    @InjectMocks
    private CommentServiceImpl commentService;

    @Test
    @DisplayName("댓글 작성 - 성공")
    void create_comment_success() {

        CommentRequestDto dto = createCommentRequest("test content");
        User user = createUser(1L, "test@test.com", "test");
        Post post = createPost(1L, user, "test title", "test content");

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        when(commentRepository.save(any()))
                .thenAnswer(invocation -> {
                    Comment comment = invocation.getArgument(0);
                    ReflectionTestUtils.setField(comment, "id", 1L);
                    return comment;
                });

        CommentResponse response = commentService.createComment(dto, 1L, user);
        assertThat(response.getContent()).isEqualTo("test content");

        verify(postRepository).findById(post.getId());
        verify(commentRepository).save(any(Comment.class));
        verify(commentRepository).save(argThat(
                c -> c.getPost().getId().equals(1L) && c.getUser().getId().equals(1L)
        ));
    }

    @Test
    @DisplayName("댓글 작성 - 실패(사용자 없음)")
    void create_comment_fail() {

        CommentRequestDto dto = createCommentRequest("test content");
        User user = createUser(1L, "test@test.com", "test");
        Post post = createPost(1L, user, "test title", "test content");

        when(postRepository.findById(post.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.createComment(dto, post.getId(), user))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(postRepository).findById(post.getId());
    }

    @Test
    @DisplayName("댓글 목록 조회(게시글별) - 성공")
    void get_comments_post() {
        Pageable pageable = PageRequest.of(0, 10);
        User user = createUser(1L, "test@test.com", "test");
        Post post = createPost(1L, user, "test title", "test content");

        Comment comment = createComment(1L, user, post, "test content");
        Comment comment1 = createComment(2L, user, post, "test content");

        when(commentRepository.findAllByPostIdWithUser(post.getId(), pageable))
                .thenReturn(new PageImpl<>(List.of(comment, comment1)));

        Page<CommentResponse> response = commentService.getCommentByPost(post.getId(), pageable);

        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getContent().getFirst().getContent())
                .isEqualTo("test content");

        verify(commentRepository).findAllByPostIdWithUser(post.getId(), pageable);
    }

    @Test
    @DisplayName("댓글 목록 조회(사용자별) - 성공")
    void get_comments_user() {
        Pageable pageable = PageRequest.of(0, 3);
        User user = createUser(1L, "test@test.com", "test");
        Post post = createPost(1L, user, "test title", "test content");

        Comment comment = createComment(1L, user, post, "test content");
        Comment comment1 = createComment(2L, user, post, "test content");

        when(commentRepository.findAllByUser(user, pageable))
                .thenReturn(new PageImpl<>(List.of(comment, comment1)));

        Page<CommentResponse> response = commentService.getCommentByUser(user, pageable);

        assertThat(response.getContent().getFirst().getContent())
                .isEqualTo("test content");
        assertThat(response.getContent()).hasSize(2);

        verify(commentRepository).findAllByUser(user, pageable);
    }

    @Test
    @DisplayName("댓글 조회 - 성공")
    void get_comment_success() {
        User user = createUser(1L, "test@test.com", "test");
        Post post = createPost(1L, user, "test title", "test content");

        Comment comment = createComment(1L, user, post, "test content");

        when(commentRepository.findByIdWithUser(comment.getId()))
                .thenReturn(Optional.of(comment));

        CommentResponse response = commentService.getComment(comment.getId());

        assertThat(response.getContent()).isEqualTo("test content");
        verify(commentRepository).findByIdWithUser(comment.getId());
    }

    @Test
    @DisplayName("댓글 조회 - 실패")
    void get_comment_fail() {
        Long wrongId = 100L;

        when(commentRepository.findByIdWithUser(wrongId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.getComment(wrongId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(commentRepository).findByIdWithUser(wrongId);
    }

    @Test
    @DisplayName("댓글 수정 - 성공")
    void update_comment_success() {
        User user = createUser(1L, "test@test.com", "test");
        Post post = createPost(1L, user, "test title", "test content");

        Comment comment = createComment(1L, user, post, "test content");

        CommentRequestDto dto = CommentRequestDto.builder()
                .content("update content")
                .build();

        when(commentRepository.findByIdWithUser(comment.getId()))
                .thenReturn(Optional.of(comment));

        doNothing().when(authValidator).validate(user, user);

        CommentResponse response = commentService.update(dto, comment.getId(), user);

        assertThat(response.getContent()).isEqualTo("update content");
        verify(commentRepository).findByIdWithUser(comment.getId());
        verify(authValidator).validate(user, user);
    }

    @Test
    @DisplayName("댓글 수정 - 실패(존재하지 않는 댓글)")
    void update_comment_fail() {
        Long wrongId = 100L;

        User user = createUser(1L, "test@test.com", "test");

        CommentRequestDto dto = CommentRequestDto.builder()
                .content("update content")
                .build();

        when(commentRepository.findByIdWithUser(wrongId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.update(dto, wrongId, user))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(commentRepository).findByIdWithUser(wrongId);
        verify(authValidator, never()).validate(user, user);
    }

    @Test
    @DisplayName("댓글 삭제 - 성공")
    void delete_comment_success() {
        User user = createUser(1L, "test@test.com", "test");
        Post post = createPost(1L, user, "test title", "test content");

        Comment comment = createComment(1L, user, post, "test content");

        commentService.delete(comment.getId());

        verify(commentRepository).deleteById(comment.getId());
    }

    private CommentRequestDto createCommentRequest(String content) {

        return CommentRequestDto.builder()
                .content(content)
                .build();
    }

    private User createUser(Long id, String email, String nickname) {

        User user = User.builder()
                .email(email)
                .password("1234")
                .nickname(nickname)
                .profileImage("img")
                .build();

        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Post createPost(Long id, User user, String title, String content) {

        Post post = Post.builder()
                .title(title)
                .content(content)
                .user(user)
                .build();

        ReflectionTestUtils.setField(post, "id", id);
        return post;
    }

    private Comment createComment(Long id, User user, Post post, String content) {

        Comment comment = Comment.builder()
                .content(content)
                .user(user)
                .post(post)
                .build();

        ReflectionTestUtils.setField(comment, "id", id);
        return comment;
    }
}
