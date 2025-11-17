package com.example.week7.dto.response.user;

import com.example.week7.common.util.DateTimeUtils;
import com.example.week7.domain.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor

/**
 * 패스워드 삭제할것.. 확인용..
 */
public class UserDetailResponse {

    private String email;
    private String nickname;
    private String createdDate;
    private String profileImage;

    @Builder
    public UserDetailResponse(String email, String nickname, String createdDate, String profileImage) {
        this.email = email;
        this.nickname = nickname;
        this.createdDate = createdDate;
        this.profileImage = profileImage;
    }

    public static UserDetailResponse fromEntity(User user) {
        return UserDetailResponse.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .createdDate(DateTimeUtils.format(user.getCreatedDate()))
                .profileImage(user.getProfileImage())
                .build();

    }

}
