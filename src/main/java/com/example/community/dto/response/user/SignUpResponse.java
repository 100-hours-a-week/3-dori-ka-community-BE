package com.example.community.dto.response.user;

import com.example.community.domain.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignUpResponse {

    private String email;
    private String profileImage;

    @Builder
    public SignUpResponse(String email, String profileImage) {
        this.email = email;
        this.profileImage = profileImage;
    }

    public static SignUpResponse fromEntity(User user) {
        return SignUpResponse
                .builder()
                .email(user.getEmail())
                .profileImage(user.getProfileImage())
                .build();
    }
}
