package com.example.community.controller;

import com.example.community.dto.response.post.PostImageResponse;
import com.example.community.security.jwt.JwtAuthenticationFilter;
import com.example.community.service.post.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostImageController.class)
@AutoConfigureMockMvc(addFilters = false)
class PostImageControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Long POST_ID = 1L;

    @Test
    @DisplayName("게시글 이미지 조회 - 성공")
    void get_post_images_success() throws Exception{

        PostImageResponse response = PostImageResponse.builder()
                .postImageId(1L)
                .postImageUrl("post/dummy1-uuid")
                .build();

        PostImageResponse response1 = PostImageResponse.builder()
                .postImageId(2L)
                .postImageUrl("post/dummy2-uuid")
                .build();

        when(postService.getAllPostImageByPostId(anyLong()))
                .thenReturn(List.of(response, response1));

        mockMvc.perform(get("/posts/{id}/images", POST_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("게시글 이미지 조회 성공"))
                .andExpect(jsonPath(("$.data[0].postImageId")).value(1L))
                .andExpect(jsonPath(("$.data[1].postImageId")).value(2L))
                .andExpect(jsonPath(("$.data[0].postImageUrl")).value("post/dummy1-uuid"))
                .andExpect(jsonPath(("$.data[1].postImageUrl")).value("post/dummy2-uuid"));

        verify(postService).getAllPostImageByPostId(anyLong());
    }

}