package com.example.community.dto.response.user;

import com.example.community.domain.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignUpResponse {

    private String email;

    @Builder
    public SignUpResponse(String email) {
        this.email = email;
    }

    public static SignUpResponse fromEntity(User user) {
        return SignUpResponse
                .builder()
                .email(user.getEmail())
                .build();
    }
}
