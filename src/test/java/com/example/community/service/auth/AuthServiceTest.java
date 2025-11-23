package com.example.community.service.auth;

import com.example.community.common.exception.custom.BadRequestException;
import com.example.community.domain.RefreshToken;
import com.example.community.domain.User;
import com.example.community.dto.request.user.UserLoginDto;
import com.example.community.dto.request.user.UserSignUpDto;
import com.example.community.dto.response.user.LoginResponse;
import com.example.community.repository.token.RefreshTokenRepository;
import com.example.community.repository.user.UserRepository;
import com.example.community.service.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("로그인 - 성공")
    void login_success() {

        //given
        UserSignUpDto signUpRequest = UserSignUpDto.builder()
                .email("test@test.co.kr")
                .password("1234")
                .passwordCheck("1234")
                .nickname("dori")
                .profileImage("")
                .build();

        userService.signUp(signUpRequest);

        UserLoginDto request = UserLoginDto.builder()
                .email("test@test.co.kr")
                .password("1234")
                .build();


        //when
        LoginResponse response = authService.login(request.getEmail(), request.getPassword());

        //then
        assertThat(response.getEmail()).isEqualTo(request.getEmail());
        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getRefreshToken()).isNotBlank();
    }

    @Test
    @DisplayName("로그인 - 실패, 비밀번호 불일치")
    void login_fail() {
        //given
        UserSignUpDto signUpRequest = UserSignUpDto.builder()
                .email("test@test.co.kr")
                .password("1234")
                .passwordCheck("1234")
                .nickname("dori")
                .profileImage("")
                .build();

        userService.signUp(signUpRequest);

        //when
        UserLoginDto request = UserLoginDto.builder()
                .email("test@test.co.kr")
                .password("12345")
                .build();

        //then
        assertThatThrownBy(() -> authService.login(request.getEmail(), request.getPassword()))
                .isInstanceOf(AuthenticationException.class);

    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_success() {

        // given
        UserSignUpDto signUpRequest = UserSignUpDto.builder()
                .email("test@test.co.kr")
                .password("1234")
                .passwordCheck("1234")
                .nickname("dori")
                .profileImage("")
                .build();

        userService.signUp(signUpRequest);

        UserLoginDto loginRequest = UserLoginDto.builder()
                .email("test@test.co.kr")
                .password("1234")
                .build();

        LoginResponse response = authService.login(loginRequest.getEmail(), loginRequest.getPassword());

        User user = userRepository.findByEmail("test@test.co.kr").orElseThrow();
        assertThat(refreshTokenRepository.findByUser(user)).isPresent();

        // when
        authService.logout(response.getAccessToken());

        // then
        Optional<RefreshToken> token = refreshTokenRepository.findByUser(user);
        assertThat(token).isNotPresent();
    }
}