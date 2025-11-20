package com.example.community.service.auth;

import com.example.community.common.exception.custom.UnauthorizedException;
import com.example.community.security.CustomUserDetails;
import com.example.community.security.jwt.JwtUtil;
import com.example.community.domain.RefreshToken;
import com.example.community.domain.User;
import com.example.community.dto.response.user.LoginResponse;
import com.example.community.repository.token.RefreshTokenRepository;
import com.example.community.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.example.community.common.exception.ErrorMessage.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    public LoginResponse login(String email, String password) {

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, password);
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);
        CustomUserDetails userDetails = (CustomUserDetails) authenticate.getPrincipal();
        User user = userDetails.getUser();

        String accessToken = jwtUtil.createAccessToken(user);

        String refreshToken = jwtUtil.createRefreshToken(user);

        long refreshExpiration = jwtUtil.getRefreshExpiration();

        RefreshToken exist = refreshTokenRepository.findByUser(user).orElse(null);
        LocalDateTime expirationDate = LocalDateTime.now().plusSeconds(refreshExpiration);

        if (exist != null) {
            exist.updateToken(refreshToken, expirationDate);
        } else {
            RefreshToken token = RefreshToken.builder()
                    .refreshToken(refreshToken)
                    .user(user)
                    .expirationDate(expirationDate)
                    .build();

            refreshTokenRepository.save(token);
        }

        return LoginResponse.fromEntity(user, accessToken, refreshToken);

    }

    @Override
    public LoginResponse validateRefreshToken(String token) {

        if (jwtUtil.isInvalidToken(token)) {
            throw new UnauthorizedException(TOKEN_EXPIRE);
        }

        String email = jwtUtil.getEmail(token);

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UnauthorizedException(UNAUTHORIZED)
        );

        RefreshToken refreshToken = refreshTokenRepository.findByUser(user).orElseThrow(
                () -> new UnauthorizedException(TOKEN_EXPIRE)
        );


        if (!refreshToken.getRefreshToken().equals(token)) {
            throw new UnauthorizedException(UNAUTHORIZED);
        }

        String accessToken = jwtUtil.createAccessToken(user);
        return LoginResponse.fromEntity(user, accessToken, refreshToken.getRefreshToken());
    }

    @Override
    public void logout(String token) {
        String email = jwtUtil.getEmail(token);

        if (email == null) {
            throw new UnauthorizedException(UNAUTHORIZED);
        }

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UnauthorizedException(UNAUTHORIZED)
        );

        RefreshToken refreshToken = refreshTokenRepository.findByUser(user).orElseThrow(
                () -> new UnauthorizedException(TOKEN_EXPIRE)
        );


        refreshTokenRepository.delete(refreshToken);
    }
}
