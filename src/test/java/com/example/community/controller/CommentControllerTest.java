package com.example.community.controller;

import com.example.community.common.exception.custom.ResourceNotFoundException;
import com.example.community.dto.request.comment.CommentRequestDto;
import com.example.community.dto.response.comment.CommentResponse;
import com.example.community.security.jwt.JwtAuthenticationFilter;
import com.example.community.service.comment.CommentServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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

@WebMvcTest(CommentController.class)
@AutoConfigureMockMvc(addFilters = false)
class CommentControllerTest {

    public static final Long POST_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CommentServiceImpl commentService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("댓글 작성 - 성공")
    void create_comment_success() throws Exception {
        CommentRequestDto dto = CommentRequestDto.builder()
                .content("test content")
                .build();

        CommentResponse response = CommentResponse.builder()
                .commentId(1L)
                .postId(POST_ID)
                .content("test content")
                .writer("test")
                .createdDate("0000-01-01")
                .build();

        when(commentService.createComment(any(CommentRequestDto.class), any(), any()))
                .thenReturn(response);

        mockMvc.perform(post("/posts/{postId}/comments", POST_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("댓글 작성 성공"))
                .andExpect(jsonPath("$.data.commentId").value(1L))
                .andExpect(jsonPath("$.data.postId").value(1L))
                .andExpect(jsonPath("$.data.content").value("test content"))
                .andExpect(jsonPath("$.data.writer").value("test"))
                .andExpect(jsonPath("$.data.createdDate").value("0000-01-01"));

        verify(commentService).createComment(any(CommentRequestDto.class), eq(POST_ID), any());
    }

    @Test
    @DisplayName("댓글 작성 - 실패(존재하지 않는 게시물)")
    void create_comment_fail() throws Exception{

        CommentRequestDto dto = CommentRequestDto.builder()
                .content("test content")
                .build();

        when(commentService.createComment(any(CommentRequestDto.class), any(), any()))
                .thenThrow(new ResourceNotFoundException(RESOURCE_NOT_FOUND));

        mockMvc.perform(post("/posts/{postId}/comments", POST_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                .header("Authorization", "Bearer dummy-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 페이지입니다."));

        verify(commentService).createComment(any(CommentRequestDto.class), eq(POST_ID), any());
    }

    @Test
    @DisplayName("댓글 목록 조회 - 성공")
    void get_comments_success() throws Exception {
        CommentResponse comment = CommentResponse.builder()
                .commentId(1L)
                .postId(POST_ID)
                .content("test content")
                .writer("test")
                .createdDate("0000-01-01")
                .build();

        CommentResponse comment1 = CommentResponse.builder()
                .commentId(2L)
                .postId(POST_ID)
                .content("test content 1")
                .writer("test1")
                .createdDate("0000-01-01")
                .build();

        Pageable pageable = PageRequest.of(0, 10);

        PageImpl<CommentResponse> page = new PageImpl<>(List.of(comment, comment1));

        when(commentService.getCommentByPost(any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/posts/{postId}/comments", POST_ID)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("댓글 조회 성공"))
                .andExpect(jsonPath("$.data.content[0].content").value("test content"))
                .andExpect(jsonPath("$.data.content[1].content").value("test content 1"))
                .andExpect(jsonPath("$.data.content.length()").value(2));

        verify(commentService).getCommentByPost(eq(POST_ID), any(Pageable.class));
    }

    @Test
    @DisplayName("댓글 수정 - 성공")
    void update_comment_success() throws Exception {

        Long commentId = 1L;

        CommentRequestDto dto = CommentRequestDto.builder()
                .content("update content")
                .build();

        CommentResponse response = CommentResponse.builder()
                .commentId(1L)
                .postId(POST_ID)
                .content("update content")
                .writer("test")
                .createdDate("0000-01-01")
                .modifiedDate("0001-01-01")
                .build();

        when(commentService.update(any(CommentRequestDto.class), any(), any()))
                .thenReturn(response);

        mockMvc.perform(patch("/posts/{postId}/comments/{commentId}", POST_ID, commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("댓글 수정 성공"))
                .andExpect(jsonPath("$.data.content").value("update content"))
                .andExpect(jsonPath("$.data.modifiedDate").value("0001-01-01"));

        verify(commentService).update(any(CommentRequestDto.class), eq(commentId), any());
    }

}


