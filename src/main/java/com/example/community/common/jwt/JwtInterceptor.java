package com.example.community.common.jwt;

import com.example.community.common.exception.custom.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import static com.example.community.common.exception.ErrorMessage.*;

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception{

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String header = request.getHeader(jwtUtil.getHeader());

        if (header == null || !header.startsWith(jwtUtil.getPrefix())) {
            throw new UnauthorizedException(UNAUTHORIZED);
        }

        String token = header.substring(jwtUtil.getPrefix().length());

        if (jwtUtil.isInvalidToken(token)) {
            throw new UnauthorizedException(TOKEN_EXPIRE);
        }
        String email = jwtUtil.getEmail(token);
        request.setAttribute("email", email);

        return true;
    }
}
