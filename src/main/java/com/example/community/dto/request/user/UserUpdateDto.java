package com.example.community.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserUpdateDto {

    @NotBlank(message = "닉네임을 입력해주세요")
    @Size(max = 10, message = "닉네임은 최대 10자까지 가능합니다")
    private String nickname;

    private String profileImage;

    @Builder
    public UserUpdateDto(String nickname, String profileImage) {
        this.nickname = nickname;
        this.profileImage = profileImage;
    }
}
