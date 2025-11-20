package com.example.community.controller;

import com.example.community.common.exception.custom.UnauthorizedException;
import com.example.community.security.jwt.JwtUtil;
import com.example.community.common.response.APIResponse;
import com.example.community.dto.request.user.UserLoginDto;
import com.example.community.dto.response.user.LoginResponse;
import com.example.community.service.auth.AuthServiceImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.example.community.common.exception.ErrorMessage.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthServiceImpl authService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<APIResponse<LoginResponse>> login(@Valid @RequestBody UserLoginDto dto,
                                                            HttpServletResponse response) {

        LoginResponse loginUser = authService.login(dto.getEmail(), dto.getPassword());

        Cookie cookie = new Cookie("refreshToken", loginUser.getRefreshToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);


        response.addCookie(cookie);

        return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success("로그인 성공", loginUser));
    }

    @PostMapping("/refresh")
    public ResponseEntity<APIResponse<LoginResponse>> validateRefreshToken(@CookieValue("refreshToken") String refreshToken) {
        LoginResponse response = authService.validateRefreshToken(refreshToken);
        return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success("토큰 재발급 성공", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<APIResponse<Void>> logout(HttpServletRequest request, HttpServletResponse response) {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            throw new UnauthorizedException(UNAUTHORIZED);
        }

        String[] token = header.split(" ");
        authService.logout(token[1]);

        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);

        response.addCookie(cookie);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/token")
    public ResponseEntity<APIResponse<Boolean>> isInvalidToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            throw new UnauthorizedException(UNAUTHORIZED);
        }

        String[] token = header.split(" ");
        boolean invalidToken = !jwtUtil.isInvalidToken(token[1]);
        return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success("토큰 검증 성공", invalidToken));
    }
}



