package com.example.community.controller;

import com.example.community.common.exception.custom.ResourceNotFoundException;
import com.example.community.common.exception.custom.UnauthorizedException;
import com.example.community.dto.request.post.PostRequestDto;
import com.example.community.dto.request.post.PostUpdateDto;
import com.example.community.dto.response.post.PostCreateResponse;
import com.example.community.dto.response.post.PostDetailResponse;
import com.example.community.dto.response.post.PostListResponse;
import com.example.community.repository.user.UserRepository;
import com.example.community.security.jwt.JwtAuthenticationFilter;
import com.example.community.service.post.PostService;
import com.example.community.service.post.viewcount.PostViewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.example.community.common.exception.ErrorMessage.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostController.class)
@AutoConfigureMockMvc(addFilters = false)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PostViewService postViewService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;



    @Test
    @DisplayName("게시글 상세 조회 - 성공")
    void get_post_success() throws Exception {

        //given
        PostDetailResponse response = PostDetailResponse.builder()
                .postId(1L)
                .title("test title")
                .content("test content")
                .writer("test")
                .writerEmail("test@test.com")
                .profileImage("profileImage")
                .viewCount(1L)
                .createdDate("0000-01-01")
                .modifiedDate("0000-01-01")
                .build();

        when(postService.getPost(response.getPostId())).thenReturn(response);


        //when & then
        mockMvc.perform(get("/posts/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("게시글 조회 성공"))
                .andExpect(jsonPath("$.data.title").value("test title"))
                .andExpect(jsonPath("$.data.content").value("test content"))
                .andExpect(jsonPath("$.data.writer").value("test"))
                .andExpect(jsonPath("$.data.writerEmail").value("test@test.com"))
                .andExpect(jsonPath("$.data.profileImage").value("profileImage"))
                .andExpect(jsonPath("$.data.viewCount").value(1))
                .andExpect(jsonPath("$.data.createdDate").value("0000-01-01"))
                .andExpect(jsonPath("$.data.modifiedDate").value("0000-01-01"));

        verify(postService).getPost(1L);
    }

    @Test
    @DisplayName("게시글 상세 조회 - 실패")
    void get_post_fail() throws Exception {

        when(postService.getPost(100L)).thenThrow(new ResourceNotFoundException(RESOURCE_NOT_FOUND));

        mockMvc.perform(get("/posts/{id}", 100L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 페이지입니다."))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(postService).getPost(100L);
    }

    @Test
    @DisplayName("게시글 목록 조회 - 성공")
    void get_post_list_success() throws Exception {

        //given
        PostListResponse post = PostListResponse.builder()
                .postId(1L)
                .title("title")
                .content("content1")
                .writer("test")
                .profileImage("profile.png")
                .createdDate("0000-01-01")
                .viewCount(10L)
                .build();

        PostListResponse post1 = PostListResponse.builder()
                .postId(1L)
                .title("title1")
                .content("content1")
                .writer("test1")
                .profileImage("profile1.png")
                .createdDate("0000-01-01")
                .viewCount(1L)
                .build();

        Pageable pageable = PageRequest.of(0, 10);

        Page<PostListResponse> page = new PageImpl<>(List.of(post, post1));

        when(postService.getAllPost(any(Pageable.class))).thenReturn(page);

        //when & then
        mockMvc.perform(get("/posts")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("게시글 목록 조회 성공"))
                .andExpect(jsonPath("$.data.content[0].title").value("title"))
                .andExpect(jsonPath("$.data.content[1].title").value("title1"))
                .andExpect(jsonPath("$.data.content.length()").value(2));

        verify(postService).getAllPost(any(Pageable.class));
    }


    @Test
    @DisplayName("게시글 수정 - 성공")
    void update_post_success() throws Exception {

        //given
        Long postId = 1L;

        PostUpdateDto dto = PostUpdateDto.builder()
                .title("updated title")
                .content("updated content")
                .deletedImageIds(List.of())
                .newPostImageUrls(List.of())
                .build();

        PostDetailResponse response = PostDetailResponse.builder()
                .postId(postId)
                .title("update title")
                .content("update content")
                .writer("tester")
                .writerEmail("test@test.com")
                .profileImage("profile.png")
                .viewCount(10L)
                .createdDate("2024-01-01")
                .modifiedDate("2024-01-02")
                .build();

        when(postService.update(any(PostUpdateDto.class), eq(postId), any())).thenReturn(response);

        //when & then
        mockMvc.perform(patch("/posts/{id}", postId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("게시글 수정 성공"))
                .andExpect(jsonPath("$.data.title").value("update title"))
                .andExpect(jsonPath("$.data.content").value("update content"));

        verify(postService).update(any(PostUpdateDto.class), eq(postId), any());
    }

    @Test
    @DisplayName("게시글 수정 - 실패(존재하지 않는 게시물)")
    void update_post_fail() throws Exception {

        //given
        Long postId = 100L;

        PostUpdateDto dto = PostUpdateDto.builder()
                .title("updated title")
                .content("updated content")
                .deletedImageIds(List.of())
                .newPostImageUrls(List.of())
                .build();

        when(postService.update(any(PostUpdateDto.class), eq(postId), any()))
                .thenThrow(new ResourceNotFoundException(RESOURCE_NOT_FOUND));

        //when & then
        mockMvc.perform(patch("/posts/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 페이지입니다."));

    }

    @Test
    @DisplayName("게시글 작성 - 성공")
    void create_post_success() throws Exception {
        PostRequestDto dto = PostRequestDto.builder()
                .title("test title")
                .content("test content")
                .postImageUrls(List.of())
                .build();

        PostCreateResponse response = PostCreateResponse.builder()
                .postId(1L)
                .title("test title")
                .createdDate("0000-01-01")
                .build();

        when(postService.createPost(any(PostRequestDto.class), any())).thenReturn(response);

        mockMvc.perform(post("/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("게시글 작성 성공"))
                .andExpect(jsonPath("$.data.title").value("test title"))
                .andExpect(jsonPath("$.data.createdDate").value("0000-01-01"));

        verify(postService).createPost(any(PostRequestDto.class), any());
    }

    @Test
    @DisplayName("게시글 작성 - 실패")
    void create_post_fail() throws Exception {
        PostRequestDto dto = PostRequestDto.builder()
                .title("test title")
                .content("test content")
                .postImageUrls(List.of())
                .build();

        when(postService.createPost(any(PostRequestDto.class), isNull()))
                .thenThrow(new UnauthorizedException(UNAUTHORIZED));

        mockMvc.perform(post("/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("로그인 후 이용해주세요."));

        verify(postService).createPost(any(PostRequestDto.class), isNull());

    }

    @Test
    @DisplayName("게시글 삭제 - 성공")
    void delete_post_success() throws Exception {

        Long postId = 1L;

        doNothing().when(postService).delete(postId);

        mockMvc.perform(delete("/posts/{id}", postId))
                .andExpect(status().isNoContent());

        verify(postService).delete(postId);
    }

    @Test
    @DisplayName("게시글 삭제 - 실패")
    void delete_post_fail() throws Exception {

        Long postId = 100L;

        doThrow(new ResourceNotFoundException(RESOURCE_NOT_FOUND))
                .when(postService)
                .delete(postId);

        mockMvc.perform(delete("/posts/{id}", postId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 페이지입니다."));


        verify(postService).delete(postId);
    }

    @Test
    @DisplayName("게시글 조회수 조회 - 성공")
    void get_viewCount_success() throws Exception {

        Long postId = 100L;

        when(postViewService.getViewCount(postId)).thenReturn(10L);

        mockMvc.perform(get("/posts/{id}/viewcounts", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("조회수 조회 성공"))
                .andExpect(jsonPath("$.data").value(10L));

        verify(postViewService).getViewCount(postId);
    }

    @Test
    @DisplayName("게시글 조회수 조회 - 실패")
    void get_viewCount_fail() throws Exception {

        Long postId = 100L;

        when(postViewService.getViewCount(postId))
                .thenThrow(new ResourceNotFoundException(RESOURCE_NOT_FOUND));

        mockMvc.perform(get("/posts/{id}/viewcounts", postId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 페이지입니다."));

        verify(postViewService).getViewCount(postId);
    }

}
