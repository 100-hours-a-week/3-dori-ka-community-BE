package com.example.community.controller;

import com.example.community.common.exception.ErrorMessage;
import com.example.community.dto.request.user.UserLoginDto;
import com.example.community.dto.response.user.LoginResponse;
import com.example.community.security.jwt.JwtAuthenticationFilter;
import com.example.community.security.jwt.JwtUtil;
import com.example.community.service.auth.AuthServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthServiceImpl authService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("로그인 - 성공")
    void login_success() throws Exception {
        UserLoginDto dto = UserLoginDto.builder()
                .email("test@test.com")
                .password("Abcd1234!")
                .build();

        LoginResponse response = LoginResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .email("test@test.com")
                .build();

        when(authService.login(dto.getEmail(), dto.getPassword())).thenReturn(response);

        mockMvc.perform(post("/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그인 성공"))
                .andExpect(jsonPath("$.data.email").value("test@test.com"))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(cookie().value("refreshToken", "refresh-token"))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(cookie().maxAge("refreshToken", 7 * 24 * 60 * 60));

        verify(authService).login(dto.getEmail(), dto.getPassword());
    }

    @Test
    @DisplayName("리프레시 토큰 검증 - 성공")
    void validate_refreshToken_success() throws Exception {
        LoginResponse response = LoginResponse.builder()
                .accessToken("new-access-token")
                .refreshToken("refresh-token")
                .email("test@test.com")
                .build();

        when(authService.validateRefreshToken("refresh-token")).thenReturn(response);

        mockMvc.perform(post("/auth/refresh")
                        .cookie(new Cookie("refreshToken", "refresh-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("토큰 재발급 성공"))
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));

        verify(authService).validateRefreshToken("refresh-token");
    }

    @Test
    @DisplayName("로그아웃 성공 시 리프레시 토큰 쿠키를 제거한다")
    void logout_success() throws Exception {
        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer access-token"))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("refreshToken", 0));

        verify(authService).logout("access-token");
    }

    @Test
    @DisplayName("Authorization 헤더가 없으면 로그아웃은 실패한다")
    void logout_fail_missingHeader() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(ErrorMessage.UNAUTHORIZED.getMessage()));
    }

    @Test
    @DisplayName("액세스 토큰 검증 - 성공")
    void token_validation_success() throws Exception {
        when(jwtUtil.isInvalidToken("access-token")).thenReturn(false);

        mockMvc.perform(get("/auth/token")
                        .header("Authorization", "Bearer access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("토큰 검증 성공"))
                .andExpect(jsonPath("$.data").value(true));

        verify(jwtUtil).isInvalidToken("access-token");
    }
}
