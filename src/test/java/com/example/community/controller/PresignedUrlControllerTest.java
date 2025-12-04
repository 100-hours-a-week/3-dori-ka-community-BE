package com.example.community.controller;

import com.example.community.dto.request.image.PresignedUrlRequestDto;
import com.example.community.dto.response.s3.PresignedUrlResponse;
import com.example.community.security.jwt.JwtAuthenticationFilter;
import com.example.community.service.s3.PreSignedUrlService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PresignedUrlController.class)
@AutoConfigureMockMvc(addFilters = false)
class PresignedUrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PreSignedUrlService preSignedUrlService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("presigned-url 생성 - 성공")
    void create_presigned_url_success() throws Exception{
        PresignedUrlRequestDto dto = PresignedUrlRequestDto.builder()
                .prefix("prefix")
                .contentType("image/png")
                .build();

        PresignedUrlResponse response = PresignedUrlResponse.builder()
                .key("test-prefix/test-uuid")
                .presignedUrl("https://test-bucket.s3.ap-northeast-2.amazonaws.com/test-prefix/test-uuid?sig=dummy")
                .profileImageUrl("https://test-bucket.s3.ap-northeast-2.amazonaws.com/test-prefix/test-uuid")
                .build();

        when(preSignedUrlService.createdPresignedUrl(anyString(), anyString()))
                .thenReturn(response);

        mockMvc.perform(post("/presigned-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("url 생성 성공"))
                .andExpect(jsonPath(("$.data.key")).value("test-prefix/test-uuid"))
                .andExpect(jsonPath(("$.data.presignedUrl")).value("https://test-bucket.s3.ap-northeast-2.amazonaws.com/test-prefix/test-uuid?sig=dummy"))
                .andExpect(jsonPath(("$.data.profileImageUrl")).value("https://test-bucket.s3.ap-northeast-2.amazonaws.com/test-prefix/test-uuid"));

        verify(preSignedUrlService).createdPresignedUrl(anyString(), anyString());
    }

}