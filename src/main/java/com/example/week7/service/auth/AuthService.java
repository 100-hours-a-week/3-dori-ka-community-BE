package com.example.week7.service.auth;

import com.example.week7.dto.response.user.LoginResponse;
import org.springframework.stereotype.Service;

public interface AuthService {

    LoginResponse login(String email, String password);

    LoginResponse validateRefreshToken(String token);

    void logout(String token);
}
