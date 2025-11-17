package com.example.community.service.user;

import com.example.community.dto.request.user.ChangePasswordDto;
import com.example.community.dto.request.user.UserSignUpDto;
import com.example.community.dto.request.user.UserUpdateDto;
import com.example.community.dto.response.user.SignUpResponse;
import com.example.community.dto.response.user.UserDetailResponse;

public interface UserCommandService {

    SignUpResponse signUp(UserSignUpDto dto);

    UserDetailResponse getUserInfo(Long id);

    UserDetailResponse updateUser(UserUpdateDto dto, String email);

    void changePassword(ChangePasswordDto dto, String email);

    void delete(Long id);

    void delete(String email);
}
