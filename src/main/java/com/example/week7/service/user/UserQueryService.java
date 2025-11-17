package com.example.week7.service.user;

import com.example.week7.dto.response.user.UserDetailResponse;

public interface UserQueryService {
    UserDetailResponse getUserInfo(Long id);

    UserDetailResponse getUserInfoByEmail(String email);

    Boolean isEmailDuplicated(String email);

    Boolean isNicknameDuplicated(String nickname);
}

