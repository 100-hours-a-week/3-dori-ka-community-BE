package com.example.community.dto.request.user;

import com.example.community.domain.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserSignUpDto {

    @Email(message = "올바른 이메일 주소 형식을 입력해주세요")
    @NotBlank(message = "이메일을 입력해주세요")
    private String email;

    @NotBlank(message = "비밀번호를 입력해주세요")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+=\\-{}\\[\\]:;\"'<>,.?/]).{8,20}$",
            message = "비밀번호는 8자 이상, 20자 이하이며 대문자, 소문자, 숫자, 특수문자를 각각 최소 1개 포함해야 합니다."
    )
    private String password;

    @NotBlank(message = "비밀번호를 한번 더 입력해주세요")
    private String passwordCheck;

    @NotBlank(message = "닉네임을 입력해주세요")
    @Size(max = 10, message = "닉네임은 최대 10자까지 가능합니다")
    private String nickname;

    private String profileImage;

    @Builder
    public UserSignUpDto(String email, String password, String passwordCheck, String nickname, String profileImage) {
        this.email = email;
        this.password = password;
        this.passwordCheck = passwordCheck;
        this.nickname = nickname;
        this.profileImage = profileImage;
    }

    @Builder
    public static User ofEntity(UserSignUpDto dto) {
        return User.builder()
                .email(dto.getEmail())
                .password(dto.getPassword())
                .nickname(dto.getNickname())
                .profileImage(dto.profileImage)
                .build();
    }
}
