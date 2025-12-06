package com.example.community.controller;

import com.example.community.common.exception.custom.BadRequestException;
import com.example.community.common.exception.custom.DuplicatedException;
import com.example.community.common.exception.custom.ResourceNotFoundException;
import com.example.community.dto.response.post.PostLikeResponse;
import com.example.community.security.jwt.JwtAuthenticationFilter;
import com.example.community.service.post.like.PostLikeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.example.community.common.exception.ErrorMessage.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostLikeController.class)
@AutoConfigureMockMvc(addFilters = false)
class PostLikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostLikeService postLikeService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Long POST_ID = 1L;

    @Test
    @DisplayName("게시글 좋아요 - 성공")
    void add_like_success() throws Exception{

        PostLikeResponse response = PostLikeResponse.builder()
                .postId(POST_ID)
                .liked(true)
                .likeCount(1L)
                .build();

        when(postLikeService.addLike(eq(POST_ID), any()))
                .thenReturn(response);

        mockMvc.perform(post("/posts/{postId}/likes", POST_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("좋아요 생성 성공"))
                .andExpect(jsonPath("$.data.postId").value(POST_ID))
                .andExpect(jsonPath("$.data.liked").value(true))
                .andExpect(jsonPath("$.data.likeCount").value(1L));

        verify(postLikeService).addLike(eq(POST_ID), any());
    }

    @Test
    @DisplayName("게시글 좋아요 - 실패(존재하지 않는 게시글)")
    void add_like_fail_no_post() throws Exception{
        Long wrongId = 100L;

        when(postLikeService.addLike(eq(wrongId), any()))
                .thenThrow(new ResourceNotFoundException(RESOURCE_NOT_FOUND));

        mockMvc.perform(post("/posts/{postId}/likes", wrongId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 페이지입니다."));

        verify(postLikeService).addLike(eq(wrongId), any());
    }

    @Test
    @DisplayName("게시글 좋아요 - 실패(중복 좋아여)")
    void add_like_fail_duplicate() throws Exception{

        when(postLikeService.addLike(eq(POST_ID), any()))
                .thenThrow(new DuplicatedException(DUPLICATED_LIKES));

        mockMvc.perform(post("/posts/{postId}/likes", POST_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 좋아요를 눌렀습니다."));

        verify(postLikeService).addLike(eq(POST_ID), any());
    }

    @Test
    @DisplayName("게시글 좋아요 취소 - 성공")
    void remove_like_success() throws Exception{
        PostLikeResponse response = PostLikeResponse.builder()
                .postId(POST_ID)
                .liked(false)
                .likeCount(0L)
                .build();

        when(postLikeService.removeLike(eq(POST_ID), any()))
                .thenReturn(response);
        mockMvc.perform(delete("/posts/{postId}/likes", POST_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("좋아요 취소 성공"))
                .andExpect(jsonPath("$.data.postId").value(POST_ID))
                .andExpect(jsonPath("$.data.liked").value(false))
                .andExpect(jsonPath("$.data.likeCount").value(0L));

        verify(postLikeService).removeLike(eq(POST_ID), any());
    }

    @Test
    @DisplayName("게시글 좋아요 취소 - 실패(좋아요 없음)")
    void remove_like_fail() throws Exception{

        when(postLikeService.removeLike(eq(POST_ID), any()))
                .thenThrow(new BadRequestException(NOT_LIKED_POST));

        mockMvc.perform(delete("/posts/{postId}/likes", POST_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("좋아요하지 않은 게시물입니다."));

        verify(postLikeService).removeLike(eq(POST_ID), any());
    }

    @Test
    @DisplayName("게시글 좋아요 조회 - 성공")
    void get_post_like_success() throws Exception{

        PostLikeResponse response = PostLikeResponse.builder()
                .postId(POST_ID)
                .liked(true)
                .likeCount(5L)
                .build();

        when(postLikeService.getLikeCount(eq(POST_ID), any()))
                .thenReturn(response);

        mockMvc.perform(get("/posts/{postId}/likes", POST_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("좋아요 조회 성공"))
                .andExpect(jsonPath("$.data.postId").value(POST_ID))
                .andExpect(jsonPath("$.data.liked").value(true))
                .andExpect(jsonPath("$.data.likeCount").value(5L));

        verify(postLikeService).getLikeCount(eq(POST_ID), any());
    }
}