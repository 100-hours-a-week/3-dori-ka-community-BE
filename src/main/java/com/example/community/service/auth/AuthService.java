package com.example.community.service.auth;

import com.example.community.dto.response.user.LoginResponse;

public interface AuthService {

    LoginResponse login(String email, String password);

    LoginResponse validateRefreshToken(String token);

    void logout(String token);
}
