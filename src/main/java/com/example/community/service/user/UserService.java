package com.example.community.service.user;

import com.example.community.domain.User;
import com.example.community.dto.request.user.ChangePasswordDto;
import com.example.community.dto.request.user.UserSignUpDto;
import com.example.community.dto.request.user.UserUpdateDto;
import com.example.community.dto.response.user.SignUpResponse;
import com.example.community.dto.response.user.UserDetailResponse;

public interface UserService {

    SignUpResponse signUp(UserSignUpDto dto);

    UserDetailResponse updateUser(UserUpdateDto dto, User user);

    void changePassword(ChangePasswordDto dto, User user);

    void delete(Long id);

    void delete(User user);

    UserDetailResponse getUserInfoById(Long id);

    UserDetailResponse getUserInfo(User user);

    Boolean isEmailDuplicated(String email);

    Boolean isNicknameDuplicated(String nickname);
}
