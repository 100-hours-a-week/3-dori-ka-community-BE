package com.example.community.controller;

import com.example.community.dto.response.comment.CommentResponse;
import com.example.community.dto.response.post.PostListResponse;
import com.example.community.dto.response.user.UserDetailResponse;
import com.example.community.security.jwt.JwtAuthenticationFilter;
import com.example.community.service.comment.CommentService;
import com.example.community.service.post.PostService;
import com.example.community.service.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MyPageController.class)
@AutoConfigureMockMvc(addFilters = false)
class MyPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("마이페이지 사용자 정보 조회 - 성공")
    void get_user_info_success() throws Exception{

        UserDetailResponse response = UserDetailResponse.builder()
                .email("test@test.com")
                .nickname("tester")
                .createdDate("2024-01-01")
                .profileImage("profile.png")
                .build();

        when(userService.getUserInfo(any()))
                .thenReturn(response);

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message"). value("사용자 조회 성공"))
                .andExpect(jsonPath("$.data.email").value("test@test.com"))
                .andExpect(jsonPath("$.data.nickname").value("tester"))
                .andExpect(jsonPath("$.data.createdDate").value("2024-01-01"))
                .andExpect(jsonPath("$.data.profileImage").value("profile.png"));

        verify(userService).getUserInfo(any());
    }

    @Test
    @DisplayName("마이페이지 사용자 게시글 목록 조회 - 성공")
    void get_user_post_success() throws Exception{

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
                .postId(2L)
                .title("title1")
                .content("content1")
                .writer("test1")
                .profileImage("profile1.png")
                .createdDate("0000-01-01")
                .viewCount(1L)
                .build();

        PageImpl<PostListResponse> page = new PageImpl<>(List.of(post, post1));

        when(postService.getAllPostByUser(any(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/users/me/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("사용자 게시글 조회 성공"))
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[0].postId").value(1L))
                .andExpect(jsonPath("$.data.content[0].title").value("title"))
                .andExpect(jsonPath("$.data.content[1].postId").value(2L))
                .andExpect(jsonPath("$.data.content[1].title").value("title1"));

        verify(postService).getAllPostByUser(any(), any(Pageable.class));
    }

    @Test
    @DisplayName("마이페이지 사용자 댓글 조회 - 성공")
    void get_user_comments_success() throws Exception{

        CommentResponse comment = CommentResponse.builder()
                .commentId(1L)
                .postId(1L)
                .content("test comment")
                .writer("tester")
                .writerEmail("test@test.com")
                .createdDate("0000-01-01")
                .createdDate("1111-01-01")
                .build();

        CommentResponse comment1 = CommentResponse.builder()
                .commentId(2L)
                .postId(1L)
                .content("test comment2")
                .writer("tester")
                .writerEmail("test@test.com")
                .createdDate("0000-01-02")
                .createdDate("1111-01-02")
                .build();

        PageImpl<CommentResponse> page = new PageImpl<>(List.of(comment, comment1));

        when(commentService.getCommentByUser(any(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/users/me/comments")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("사용자 댓글 조회 성공"))
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[0].commentId").value(1L))
                .andExpect(jsonPath("$.data.content[0].content").value("test comment"))
                .andExpect(jsonPath("$.data.content[1].commentId").value(2L))
                .andExpect(jsonPath("$.data.content[1].content").value("test comment2"));

        verify(commentService).getCommentByUser(any(), any(Pageable.class));
    }

    @Test
    @DisplayName("마이페이지 사용자 정보 수정 성공")
    void update_user_profile_success() {

    }
}