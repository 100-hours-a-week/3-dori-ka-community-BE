package com.example.community.service.user;

import com.example.community.dto.response.user.UserDetailResponse;

public interface UserQueryService {
    UserDetailResponse getUserInfo(Long id);

    UserDetailResponse getUserInfoByEmail(String email);

    Boolean isEmailDuplicated(String email);

    Boolean isNicknameDuplicated(String nickname);
}

