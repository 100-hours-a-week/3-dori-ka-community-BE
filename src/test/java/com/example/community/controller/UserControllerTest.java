package com.example.community.controller;

import com.example.community.common.exception.ErrorMessage;
import com.example.community.common.exception.custom.DuplicatedException;
import com.example.community.dto.request.user.UserSignUpDto;
import com.example.community.dto.response.user.SignUpResponse;
import com.example.community.dto.response.user.UserDetailResponse;
import com.example.community.security.jwt.JwtAuthenticationFilter;
import com.example.community.service.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("회원가입 - 성공")
    void signUp_success() throws Exception {

        UserSignUpDto dto = UserSignUpDto.builder()
                .email("test@test.com")
                .password("Abcd1234!")
                .passwordCheck("Abcd1234!")
                .nickname("test")
                .profileImage("profile.png")
                .build();

        SignUpResponse response = new SignUpResponse("test@test.com");

        when(userService.signUp(any(UserSignUpDto.class))).thenReturn(response);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("회원가입 성공"))
                .andExpect(jsonPath("$.data.email").value("test@test.com"));

        verify(userService, times(1)).signUp(any(UserSignUpDto.class));
    }

    @ParameterizedTest
    @MethodSource("invalidSignUpProvider")
    @DisplayName("회원가입- 실패(필수 정보 미입력)")
    void signUp_fail_missing_info(UserSignUpDto dto, String expectedMessage) throws Exception {

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(expectedMessage));

        verify(userService, never()).signUp(any());
    }

    @Test
    @DisplayName("회원가입- 실패(이메일 중복)")
    void signUp_fail_email_duplicate() throws Exception {

        UserSignUpDto dto = UserSignUpDto.builder()
                .email("test@test.com")
                .password("Abcd1234!")
                .passwordCheck("Abcd1234!")
                .nickname("test")
                .profileImage("profile.png")
                .build();

        when(userService.signUp(any(UserSignUpDto.class)))
                .thenThrow(new DuplicatedException(ErrorMessage.EMAIL_DUPLICATED));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(ErrorMessage.EMAIL_DUPLICATED.getMessage()));

        verify(userService).signUp(any(UserSignUpDto.class));
    }


    @Test
    @DisplayName("회원가입 - 실패(닉네임 중복)")
    void signUp_fail_nickname_duplicate() throws Exception {

        UserSignUpDto dto = UserSignUpDto.builder()
                .email("test@test.com")
                .password("Abcd1234!")
                .passwordCheck("Abcd1234!")
                .nickname("test")
                .profileImage("profile.png")
                .build();

        when(userService.signUp(any(UserSignUpDto.class)))
                .thenThrow(new DuplicatedException(ErrorMessage.NICKNAME_DUPLICATED));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(ErrorMessage.NICKNAME_DUPLICATED.getMessage()));

        verify(userService).signUp(any(UserSignUpDto.class));
    }

    @Test
    @DisplayName("회원가입 - 실패(이메일 형식 오류)")
    void signUp_fail_invalid_email() throws Exception {
        UserSignUpDto dto = UserSignUpDto.builder()
                .email("invalid-email")
                .password("Abcd1234!")
                .passwordCheck("Abcd1234!")
                .nickname("tester")
                .profileImage("profile.png")
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("올바른 이메일 주소 형식을 입력해주세요"));

        verify(userService, never()).signUp(any(UserSignUpDto.class));
    }

    @Test
    @DisplayName("회원가입 - 실패(닉네임 길이 오류)")
    void signUp_fail_invalid_nick_length() throws Exception {
        UserSignUpDto dto = UserSignUpDto.builder()
                .email("test@test.com")
                .password("Abcd1234!")
                .passwordCheck("Abcd1234!")
                .nickname("123456789101")
                .profileImage("profile.png")
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("닉네임은 최대 10자까지 가능합니다"));

        verify(userService, never()).signUp(any(UserSignUpDto.class));
    }

    @ParameterizedTest
    @CsvSource({
            "1234567, 길이가 8자 미만",
            "123456789012345678901, 길이가 20자 초과",
            "abcdefgh, 소문자만",
            "ABCDEFGH, 대문자만",
            "12345678, 숫자만",
            "!!!!@@@@, 특수문자만"
    })
    @DisplayName("회원가입 - 실패(비밀번호 규칙/길이)")
    void signUp_fail_invalid_password_rule(String invalidPassword) throws Exception {

        UserSignUpDto dto = UserSignUpDto.builder()
                .email("test@test.com")
                .password(invalidPassword)
                .passwordCheck(invalidPassword)
                .nickname("tester")
                .profileImage("profile.png")
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("비밀번호는 8자 이상, 20자 이하이며 대문자, 소문자, 숫자, 특수문자를 각각 최소 1개 포함해야 합니다."));

        verify(userService, never()).signUp(any(UserSignUpDto.class));
    }

    @Test
    @DisplayName("단일 회원 조회 성공")
    void getUserInfo_success() throws Exception {
        UserDetailResponse response = UserDetailResponse.builder()
                .email("test@test.com")
                .nickname("tester")
                .createdDate("2024-01-01")
                .profileImage("profile.png")
                .build();

        when(userService.getUserInfoById(1L)).thenReturn(response);

        mockMvc.perform(get("/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("조회 성공"))
                .andExpect(jsonPath("$.data.email").value("test@test.com"))
                .andExpect(jsonPath("$.data.nickname").value("tester"));

        verify(userService).getUserInfoById(1L);
    }


    @Test
    @DisplayName("이메일 중복 체크 성공")
    void emailDuplicated_success() throws Exception {
        when(userService.isEmailDuplicated("test@test.com")).thenReturn(true);

        mockMvc.perform(get("/users/email")
                        .param("email", "test@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("이메일 중복 체크 성공"))
                .andExpect(jsonPath("$.data").value(true));

        verify(userService).isEmailDuplicated("test@test.com");
    }

    @Test
    @DisplayName("닉네임 중복 체크 성공")
    void nicknameDuplicated_success() throws Exception {
        when(userService.isNicknameDuplicated("tester")).thenReturn(false);

        mockMvc.perform(get("/users/nickname")
                        .param("nickname", "tester"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("닉네임 중복 체크 성공"))
                .andExpect(jsonPath("$.data").value(false));

        verify(userService).isNicknameDuplicated("tester");
    }

    @Test
    @DisplayName("회원 삭제 성공")
    void delete_success() throws Exception {
        mockMvc.perform(delete("/users/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(userService).delete(1L);
    }

    static Stream<Arguments> invalidSignUpProvider() {
        return Stream.of(
                Arguments.of(
                        UserSignUpDto.builder().email("").password("ValidPass1!").passwordCheck("ValidPass1!").nickname("tester").build(),
                        "이메일을 입력해주세요"
                ),
                Arguments.of(
                        UserSignUpDto.builder().email("invalid").password("ValidPass1!").passwordCheck("ValidPass1!").nickname("tester").build(),
                        "올바른 이메일 주소 형식을 입력해주세요"
                ),
                Arguments.of(
                        UserSignUpDto.builder().email("test@test.com").password("").passwordCheck("ValidPass1!").nickname("tester").build(),
                        "비밀번호를 입력해주세요"
                ),
                Arguments.of(
                        UserSignUpDto.builder().email("test@test.com").password("ValidPass1!").passwordCheck("").nickname("tester").build(),
                        "비밀번호를 한번 더 입력해주세요"
                ),
                Arguments.of(
                        UserSignUpDto.builder().email("test@test.com").password("Short1!").passwordCheck("Short1!").nickname("tester").build(),
                        "비밀번호는 8자 이상, 20자 이하이며 대문자, 소문자, 숫자, 특수문자를 각각 최소 1개 포함해야 합니다."
                ),
                Arguments.of(
                        UserSignUpDto.builder().email("test@test.com").password("ValidPass1!").passwordCheck("ValidPass1!").nickname("").build(),
                        "닉네임을 입력해주세요"
                )
        );
    }
}
