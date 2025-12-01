package com.example.community.service.auth;

import com.example.community.domain.RefreshToken;
import com.example.community.domain.User;
import com.example.community.dto.response.user.LoginResponse;
import com.example.community.repository.token.RefreshTokenRepository;
import com.example.community.repository.user.UserRepository;
import com.example.community.security.CustomUserDetails;
import com.example.community.security.jwt.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("로그인 - 성공")
    void login_success() {
        User user = buildUser("test@test.co.kr");
        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtUtil.createAccessToken(user)).thenReturn("access-token");
        when(jwtUtil.createRefreshToken(user)).thenReturn("refresh-token");
        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.empty());

        LoginResponse response = authService.login(user.getEmail(), "1234");

        assertThat(response.getEmail()).isEqualTo(user.getEmail());
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("로그인 - 실패, 비밀번호 불일치")
    void login_fail() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad credentials"));

        assertThatThrownBy(() -> authService.login("test@test.co.kr", "wrong"))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_success() {
        User user = buildUser("test@test.co.kr");
        RefreshToken refreshToken = RefreshToken.builder()
                .refreshToken("refresh-token")
                .user(user)
                .expirationDate(LocalDateTime.now().plusDays(1))
                .build();

        when(jwtUtil.getEmail("access-token")).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.of(refreshToken));

        authService.logout("access-token");

        verify(refreshTokenRepository).delete(refreshToken);
    }

    private User buildUser(String email) {
        return User.builder()
                .email(email)
                .password("encoded")
                .nickname("tester")
                .profileImage("")
                .build();
    }
}
