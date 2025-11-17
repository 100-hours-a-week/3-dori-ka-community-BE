package com.example.week7.dto.response.user;

import com.example.week7.domain.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String email;

    @Builder
    public LoginResponse(String accessToken, String refreshToken, String email) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.email = email;
    }

    public static LoginResponse fromEntity(User user, String accessToken, String refreshToken) {
        return LoginResponse
                .builder()
                .refreshToken(refreshToken)
                .accessToken(accessToken)
                .email(user.getEmail())
                .build();
    }
}
