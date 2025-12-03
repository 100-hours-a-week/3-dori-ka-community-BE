package com.example.community.service.post;

import com.example.community.common.exception.ErrorMessage;
import com.example.community.common.exception.custom.ForbiddenException;
import com.example.community.common.exception.custom.ResourceNotFoundException;
import com.example.community.common.exception.custom.UnauthorizedException;
import com.example.community.common.util.AuthValidator;
import com.example.community.domain.Post;
import com.example.community.domain.PostImage;
import com.example.community.domain.User;
import com.example.community.dto.request.post.PostRequestDto;
import com.example.community.dto.request.post.PostUpdateDto;
import com.example.community.dto.response.post.PostCreateResponse;
import com.example.community.dto.response.post.PostDetailResponse;
import com.example.community.dto.response.post.PostImageResponse;
import com.example.community.dto.response.post.PostListResponse;
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
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import java.util.*;

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
        PostRequestDto dto = createPostRequestDto("test title", "test content",
                List.of("postImage1", "postImage2"));
        User user = createUser(1L, "test@test.com", "test");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(postRepository.save(any()))
                .thenAnswer(invocation -> {
                    Post saved = invocation.getArgument(0);
                    ReflectionTestUtils.setField(saved, "id", 1L);
                    return saved;
                });

        PostCreateResponse response = postService.createPost(dto, user);

        assertThat(response.getTitle()).isEqualTo("test title");

        verify(userRepository).findById(user.getId());
        verify(postRepository).save(any(Post.class));
        verify(postImageRepository, times(dto.getPostImageUrls().size())).save(any(PostImage.class));
        verifyNoInteractions(postViewService, s3Client);
    }

    @Test
    @DisplayName("게시글 작성 - 실패")
    void create_post_fail() {
        PostRequestDto dto = createPostRequestDto("test title", "test content",
                List.of("postImage1", "postImage2"));
        User user = createUser(1L, "test@test.com", "test");

        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.createPost(dto, user))
                .isInstanceOf(UnauthorizedException.class);

        verify(postRepository, never()).save(any());
        verify(postImageRepository, never()).save(any());
        verifyNoInteractions(postViewService, s3Client);
    }

    @Test
    @DisplayName("게시글 작성 실패 - 사용자 없음")
    void create_post_fail_when_user_null() {
        PostRequestDto dto = createPostRequestDto("title", "content", null);

        assertThatThrownBy(() -> postService.createPost(dto, null))
                .isInstanceOf(UnauthorizedException.class);

        verifyNoInteractions(postRepository, postImageRepository, s3Client, postViewService);
    }

    @Test
    @DisplayName("게시글 수정 - 성공")
    void update_post_success() {
        User user = createUser(1L, "test@test.com", "test");
        Post post = createPost(1L, user, "old title", "old content");
        PostUpdateDto dto = createPostUpdateDto("new title", "new content",
                List.of(), List.of());

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        doNothing().when(authValidator).validate(user, user);

        PostDetailResponse response = postService.update(dto, post.getId(), user);

        assertThat(response.getTitle()).isEqualTo("new title");

        verify(postRepository).findById(post.getId());
        verify(authValidator).validate(user, user);
        verify(postRepository, never()).save(any());
        verifyNoInteractions(postImageRepository, s3Client);
    }

    @Test
    @DisplayName("게시글 수정 - 실패(사용자 불일치)")
    void update_post_fail_different_user() {
        User owner = createUser(1L, "owner@test.com", "owner");
        User attacker = createUser(2L, "bad@test.com", "bad");

        Post post = createPost(1L, owner, "old", "old");

        PostUpdateDto dto = createPostUpdateDto("new", "new", List.of(), List.of());

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        doThrow(new ForbiddenException(ErrorMessage.FORBIDDEN))
                .when(authValidator).validate(attacker, owner);

        assertThatThrownBy(() -> postService.update(dto, post.getId(), attacker))
                .isInstanceOf(ForbiddenException.class);

        verify(postRepository).findById(post.getId());
        verify(authValidator).validate(attacker, owner);
        verifyNoInteractions(postImageRepository, s3Client);
    }

    @Test
    @DisplayName("게시글 수정 - 실패(존재하지 않는 게시물)")
    void update_post_fail_no_post() {
        User user = createUser(1L, "test@test.com", "test");
        PostUpdateDto dto = createPostUpdateDto("new", "new", List.of(), List.of());

        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.update(dto, 1L, user))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(postRepository).findById(1L);
        verifyNoInteractions(authValidator, postImageRepository, s3Client);
    }

    @Test
    @DisplayName("게시글 조회 - 성공")
    void get_post_success() {
        User user = createUser(1L, "test@test.com", "test");
        Post post = createPost(1L, user, "title", "content");

        when(postRepository.findByIdWithUser(post.getId())).thenReturn(Optional.of(post));

        PostDetailResponse response = postService.getPost(post.getId());

        assertThat(response.getTitle()).isEqualTo("title");
        verify(postRepository).findByIdWithUser(post.getId());
        verify(postViewService).increaseViewcount(post.getId());
    }

    @Test
    @DisplayName("게시글 조회 - 실패")
    void get_post_fail() {
        when(postRepository.findByIdWithUser(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getPost(100L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(postRepository).findByIdWithUser(100L);
        verifyNoInteractions(postViewService);
    }

    @Test
    @DisplayName("게시글 목록 조회 - 성공")
    void get_all_post_success() {
        Pageable pageable = PageRequest.of(0, 10);
        User user = createUser(1L, "test@test.com", "test");
        Post post = createPost(1L, user, "title", "content");

        when(postRepository.findAllWithUser(pageable))
                .thenReturn(new PageImpl<>(List.of(post)));

        Page<PostListResponse> response = postService.getAllPost(pageable);

        assertThat(response.getContent()).hasSize(1);
        verify(postRepository).findAllWithUser(pageable);
    }

    @Test
    @DisplayName("사용자별 게시글 조회 - 성공")
    void get_all_post_user_success() {
        Pageable pageable = PageRequest.of(0, 10);
        User user = createUser(1L, "test@test.com", "test");
        Post p1 = createPost(1L, user, "title1", "content1");
        Post p2 = createPost(2L, user, "title2", "content2");

        when(postRepository.findAllByUser(user, pageable))
                .thenReturn(new PageImpl<>(List.of(p1, p2)));

        Page<PostListResponse> response = postService.getAllPostByUser(user, pageable);

        assertThat(response.getContent()).hasSize(2);
        verify(postRepository).findAllByUser(user, pageable);
    }

    @Test
    @DisplayName("게시글 이미지 조회 - 성공")
    void get_all_post_image_success() {
        User user = createUser(1L, "test@test.com", "test");
        Post post = createPost(1L, user, "title", "content");

        PostImage img1 = PostImage.builder().post(post).postImageUrl("url1").build();
        PostImage img2 = PostImage.builder().post(post).postImageUrl("url2").build();

        when(postImageRepository.findAllByPostId(post.getId()))
                .thenReturn(List.of(img1, img2));

        List<PostImageResponse> response =
                postService.getAllPostImageByPostId(post.getId());

        assertThat(response).hasSize(2);
        verify(postImageRepository).findAllByPostId(post.getId());
    }

    @Test
    @DisplayName("게시글 삭제 - 성공")
    void delete_post_success() {

        // given
        User user = createUser(1L, "test@test.com", "tester");
        Post post = createPost(1L, user, "title", "content");

        PostImage image1 = createPostImage(1L, post, "img1.jpg");
        PostImage image2 = createPostImage(2L, post, "img2.jpg");

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(postImageRepository.findAllByPostId(post.getId()))
                .thenReturn(List.of(image1, image2));

        postService.delete(post.getId());

        verify(postRepository).findById(post.getId());
        verify(postImageRepository).findAllByPostId(post.getId());
        verify(s3Client, times(2)).deleteObject(any(DeleteObjectRequest.class));
        verify(postLikeRepository).deleteAllByPostId(post.getId());
        verify(postRepository).delete(post);
    }

    @Test
    @DisplayName("게시글 삭제 - 실패: 존재하지 않는 게시글")
    void delete_post_fail() {

        Long invalidId = 999L;

        when(postRepository.findById(invalidId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.delete(invalidId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(postRepository).findById(invalidId);
        verify(postImageRepository, never()).findAllByPostId(any());
        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
        verify(postLikeRepository, never()).deleteAllByPostId(any());
        verify(postRepository, never()).delete(any());
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

    private PostImage createPostImage(Long id, Post post, String postImageUrl) {
        PostImage postImage = PostImage.builder()
                .post(post)
                .postImageUrl(postImageUrl).build();

        ReflectionTestUtils.setField(postImage, "id", id);
        return postImage;
    }

    private PostRequestDto createPostRequestDto(String t, String c, List<String> imgs) {
        return PostRequestDto.builder().title(t).content(c).postImageUrls(imgs).build();
    }

    private PostUpdateDto createPostUpdateDto(String t, String c,
                                              List<Long> del, List<String> add) {
        return PostUpdateDto.builder()
                .title(t).content(c)
                .deletedImageIds(del).newPostImageUrls(add)
                .build();
    }
}