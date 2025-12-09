package com.example.community.service.auth;

import com.example.community.common.exception.custom.UnauthorizedException;
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
import static org.mockito.Mockito.*;

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
        when(jwtUtil.createAccessToken(user)).thenReturn("testAccessToken");
        when(jwtUtil.createRefreshToken(user)).thenReturn("testRefreshToken");
        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.empty());

        LoginResponse response = authService.login(user.getEmail(), "1234");

        assertThat(response.getEmail()).isEqualTo(user.getEmail());
        assertThat(response.getAccessToken()).isEqualTo("testAccessToken");
        assertThat(response.getRefreshToken()).isEqualTo("testRefreshToken");
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("로그인 - 성공(리프레시 토큰 갱신)")
    void login_success_update_exist_refresh_token() {
        User user = buildUser("test@test.co.kr");
        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        RefreshToken existingToken = RefreshToken.builder()
                .user(user)
                .refreshToken("oldRefreshToken")
                .expirationDate(LocalDateTime.now().minusDays(1))
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtUtil.createAccessToken(user)).thenReturn("testAccessToken");
        when(jwtUtil.createRefreshToken(user)).thenReturn("testRefreshToken");
        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.of(existingToken));

        LoginResponse response = authService.login(user.getEmail(), "1234");

        assertThat(response.getEmail()).isEqualTo(user.getEmail());
        assertThat(response.getAccessToken()).isEqualTo("testAccessToken");
        assertThat(response.getRefreshToken()).isEqualTo("testRefreshToken");
        assertThat(existingToken.getRefreshToken()).isEqualTo("testRefreshToken");
        assertThat(existingToken.getExpirationDate()).isAfter(LocalDateTime.now().minusMinutes(1));

        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("로그인 - 실패(비밀번호 불일치)")
    void login_fail() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad credentials"));

        assertThatThrownBy(() -> authService.login("test@test.co.kr", "wrong"))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("리프레시 토큰 검증 - 성공")
    void validate_refresh_token_success() {
        User user = buildUser("test@test.co.kr");
        String token = "testRefreshToken";
        String newAccessToken = "newAccessToken";

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .refreshToken(token)
                .build();

        when(jwtUtil.isInvalidToken(token)).thenReturn(false);
        when(jwtUtil.getEmail(token)).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
        when(refreshTokenRepository.findByUser(user))
                .thenReturn(Optional.of(refreshToken));
        when(jwtUtil.createAccessToken(user)).thenReturn(newAccessToken);

        LoginResponse response = authService.validateRefreshToken(token);

        assertThat(response.getEmail()).isEqualTo(user.getEmail());
        assertThat(response.getAccessToken()).isEqualTo(newAccessToken);
        assertThat(response.getRefreshToken()).isEqualTo(token);

        verify(jwtUtil).isInvalidToken(token);
        verify(jwtUtil).getEmail(token);
        verify(userRepository).findByEmail(user.getEmail());
        verify(refreshTokenRepository).findByUser(user);
        verify(jwtUtil).createAccessToken(user);
    }

    @Test
    @DisplayName("리프레시 토큰 검증 - 실패(리프레시 토큰 만료)")
    void validate_refresh_token_fail_expired_token() {
        String token = "expiredRefreshToken";

        when(jwtUtil.isInvalidToken(token))
                .thenReturn(true);

        assertThatThrownBy(() -> authService.validateRefreshToken(token))
                .isInstanceOf(UnauthorizedException.class);

        verify(jwtUtil, never()).getEmail(token);
        verify(userRepository, never()).findByEmail(anyString());
        verify(refreshTokenRepository, never()).findByUser(any(User.class));
        verify(jwtUtil, never()).createAccessToken(any(User.class));
    }


    @Test
    @DisplayName("리프레시 토큰 검증 - 실패(로그인되지 않은 사용자)")
    void validate_refresh_token_fail_not_login() {
        User user = buildUser("test@test.co.kr");
        String token = "testRefreshToken";

        when(jwtUtil.isInvalidToken(token)).thenReturn(false);
        when(jwtUtil.getEmail(token)).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.validateRefreshToken(token))
                .isInstanceOf(UnauthorizedException.class);

        verify(jwtUtil).isInvalidToken(token);
        verify(jwtUtil).getEmail(token);
        verify(userRepository).findByEmail(user.getEmail());
        verify(refreshTokenRepository, never()).findByUser(user);
        verify(jwtUtil, never()).createAccessToken(user);
    }

    @Test
    @DisplayName("리프레시 토큰 검증 - 실패(토큰 없음)")
    void validate_refresh_token_fail_no_token() {
        User user = buildUser("test@test.co.kr");
        String token = "testRefreshToken";

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .refreshToken(token)
                .build();

        when(jwtUtil.isInvalidToken(token)).thenReturn(false);
        when(jwtUtil.getEmail(token)).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
        when(refreshTokenRepository.findByUser(user))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.validateRefreshToken(token))
                .isInstanceOf(UnauthorizedException.class);

        verify(jwtUtil).isInvalidToken(token);
        verify(jwtUtil).getEmail(token);
        verify(userRepository).findByEmail(user.getEmail());
        verify(refreshTokenRepository).findByUser(user);
        verify(jwtUtil, never()).createAccessToken(user);
    }

    @Test
    @DisplayName("리프레시 토큰 검증 - 실패(토큰 불일치)")
    void validate_refresh_token_fail_token_mismatch() {
        User user = buildUser("test@test.co.kr");
        String token = "testRefreshToken";

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .refreshToken("differentRefreshToken")
                .build();

        when(jwtUtil.isInvalidToken(token)).thenReturn(false);
        when(jwtUtil.getEmail(token)).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
        when(refreshTokenRepository.findByUser(user))
                .thenReturn(Optional.of(refreshToken));

        assertThatThrownBy(() -> authService.validateRefreshToken(token))
                .isInstanceOf(UnauthorizedException.class);


        verify(jwtUtil).isInvalidToken(token);
        verify(jwtUtil).getEmail(token);
        verify(userRepository).findByEmail(user.getEmail());
        verify(refreshTokenRepository).findByUser(user);
        verify(jwtUtil, never()).createAccessToken(user);
    }

    @Test
    @DisplayName("로그아웃 - 성공")
    void logout_success() {
        User user = buildUser("test@test.co.kr");
        RefreshToken refreshToken = RefreshToken.builder()
                .refreshToken("testRefreshToken")
                .user(user)
                .expirationDate(LocalDateTime.now().plusDays(1))
                .build();

        when(jwtUtil.getEmail("testAccessToken")).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.of(refreshToken));

        authService.logout("testAccessToken");

        verify(refreshTokenRepository).delete(refreshToken);
    }

    @Test
    @DisplayName("로그아웃 - 실패(로그인 X)")
    void logout_fail_not_login() {
        String token = "testAccessToken";

        when(jwtUtil.getEmail(token)).thenReturn(null);

        assertThatThrownBy(() -> authService.logout(token))
                .isInstanceOf(UnauthorizedException.class);

        verify(jwtUtil).getEmail(token);
        verify(userRepository, never()).findByEmail(anyString());
        verify(refreshTokenRepository, never()).findByUser(any(User.class));
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
    }

    @Test
    @DisplayName("로그아웃 - 실패(인증 안된 사용자)")
    void logout_fail_no_user() {
        String token = "testAccessToken";
        User user = buildUser("test@test.co.kr");

        when(jwtUtil.getEmail(token))
                .thenReturn(user.getEmail());

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.logout(token))
                .isInstanceOf(UnauthorizedException.class);

        verify(jwtUtil).getEmail(token);
        verify(userRepository).findByEmail(user.getEmail());
        verify(refreshTokenRepository, never()).findByUser(user);
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
    }

    @Test
    @DisplayName("로그아웃 - 실패(리프레시 토큰 없음(로그인 정보 없음))")
    void logout_fail_no_refresh_token() {
        String token = "testAccessToken";
        User user = buildUser("test@test.co.kr");

        when(jwtUtil.getEmail(token))
                .thenReturn(user.getEmail());

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        when(refreshTokenRepository.findByUser(user))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.logout(token))
                .isInstanceOf(UnauthorizedException.class);

        verify(jwtUtil).getEmail(token);
        verify(userRepository).findByEmail(user.getEmail());
        verify(refreshTokenRepository).findByUser(user);
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
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
