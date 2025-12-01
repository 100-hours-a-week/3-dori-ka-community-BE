package com.example.community.service.post;

import com.example.community.common.exception.ErrorMessage;
import com.example.community.common.exception.custom.ForbiddenException;
import com.example.community.common.exception.custom.ResourceNotFoundException;
import com.example.community.common.exception.custom.UnauthorizedException;
import com.example.community.common.util.AuthValidator;
import com.example.community.domain.Post;
import com.example.community.domain.User;
import com.example.community.dto.request.post.PostRequestDto;
import com.example.community.dto.request.post.PostUpdateDto;
import com.example.community.dto.response.post.PostCreateResponse;
import com.example.community.dto.response.post.PostDetailResponse;
import com.example.community.repository.post.PostImageRepository;
import com.example.community.repository.post.PostLikeRepository;
import com.example.community.repository.post.PostRepository;
import com.example.community.repository.user.UserRepository;
import com.example.community.service.post.viewcount.PostViewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private PostImageRepository postImageRepository;
    @Mock
    private PostLikeRepository postLikeRepository;
    @Mock
    private PostViewService postViewService;
    @Mock
    private AuthValidator authValidator;
    @Mock
    private S3Client s3Client;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PostServiceImpl postService;

    @Test
    @DisplayName("게시글 작성 - 성공")
    void create_post_success() {
        // given
        PostRequestDto dto = createPostRequestDto("test title", "test content",
                List.of("postImage1", "postImage2"));

        User user = createUser(1L, "test@test.com", "test");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        when(postRepository.save(any(Post.class)))
                .thenAnswer(invocation -> {
                    Post saved = invocation.getArgument(0);
                    ReflectionTestUtils.setField(saved, "id", 1L);
                    return saved;
                });

        // when
        PostCreateResponse response = postService.createPost(dto, user);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("test title");
    }

    @Test
    @DisplayName("게시글 작성 - 실패")
    void create_post_fail() {
        //given
        PostRequestDto dto = createPostRequestDto("test title", "test content",
                List.of("postImage1", "postImage2"));

        User user = createUser(1L, "test@test.com", "test");

        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> postService.createPost(dto, user)).isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("게시글 수정 - 성공")
    void update_post_success() {
        //given
        User user = createUser(1L, "test@test.com", "test");

        Post existPost = createPost(1L, user, "old title", "old content");

        PostUpdateDto dto = createPostUpdateDto("new title", "new content",
                List.of(), List.of());

        when(postRepository.findById(existPost.getId())).thenReturn(Optional.of(existPost));
        doNothing().when(authValidator).validate(user, user);

        //when
        PostDetailResponse response = postService.update(dto, existPost.getId(), user);

        //then
        assertThat(response.getTitle()).isEqualTo("new title");
        assertThat(response.getContent()).isEqualTo("new content");
    }

    @Test
    @DisplayName("게시글 수정 - 실패(사용자 불일치)")
    void update_post_fail_different_user() {
        //given
        User user = createUser(1L, "test@test.com", "test");

        User diffUser = createUser(2L, "test1@test.com", "test1");

        Post existPost = createPost(1L, user, "old title", "old content");

        PostUpdateDto dto = createPostUpdateDto("new title", "new content",
                List.of(), List.of());

        when(postRepository.findById(existPost.getId())).thenReturn(Optional.of(existPost));

        //when
        doThrow(new ForbiddenException(ErrorMessage.FORBIDDEN))
                .when(authValidator)
                        .validate(diffUser, user);

        //then
        assertThatThrownBy(() -> postService.update(dto, existPost.getId(), diffUser)).isInstanceOf(ForbiddenException.class);
        verify(authValidator).validate(diffUser, user);
    }

    @Test
    @DisplayName("게시글 수정 - 실패(존재하지 않는 게시물)")
    void update_post_fail_no_post() {

        //given
        User user = createUser(1L, "test@test.com", "test");

        PostUpdateDto dto = createPostUpdateDto("new title", "new content",
                List.of(), List.of());
        //when & then
        assertThatThrownBy(() -> postService.update(dto, 1L, user)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("게시글 작성 실패 - 사용자 없음")
    void create_post_fail_when_user_null() {

        PostRequestDto dto = createPostRequestDto("title", "content", null);

        assertThatThrownBy(() -> postService.createPost(dto, null))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("게시글 조회 - 성공")
    void get_post_success() {
        // given
        User user = createUser(1L, "test@test.com", "test");
        Post post = createPost(1L, user, "title", "content");
        when(postRepository.findByIdWithUser(post.getId())).thenReturn(Optional.of(post));

        // when
        PostDetailResponse response = postService.getPost(post.getId());

        // then
        assertThat(response)
                .extracting(PostDetailResponse::getTitle,
                        PostDetailResponse::getContent,
                        PostDetailResponse::getWriter)
                .containsExactly("title", "content", user.getNickname());

        verify(postViewService).increaseViewcount(post.getId());
    }




    private User createUser(Long id, String email, String nickname) {
        User user = User.builder()
                .email(email)
                .password("1234")
                .nickname(nickname)
                .profileImage("profileImage")
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

    private PostRequestDto createPostRequestDto(String title, String content, List<String> imageUrls) {
        return PostRequestDto.builder()
                .title(title)
                .content(content)
                .postImageUrls(imageUrls)
                .build();
    }

    private PostUpdateDto createPostUpdateDto(String title, String content,
                                              List<Long> deletedImageIds, List<String> newImageUrls) {
        return PostUpdateDto.builder()
                .title(title)
                .content(content)
                .deletedImageIds(deletedImageIds)
                .newPostImageUrls(newImageUrls)
                .build();
    }

}
