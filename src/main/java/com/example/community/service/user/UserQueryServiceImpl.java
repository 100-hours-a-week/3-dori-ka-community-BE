package com.example.community.service.user;

import com.example.community.common.exception.custom.ResourceNotFoundException;
import com.example.community.common.exception.custom.UnauthorizedException;
import com.example.community.domain.User;
import com.example.community.dto.response.user.UserDetailResponse;
import com.example.community.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.community.common.exception.ErrorMessage.RESOURCE_NOT_FOUND;
import static com.example.community.common.exception.ErrorMessage.UNAUTHORIZED;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryServiceImpl implements UserQueryService {
    private final UserRepository userRepository;

    @Override
    public UserDetailResponse getUserInfo(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(RESOURCE_NOT_FOUND));
        return UserDetailResponse.fromEntity(user);
    }

    @Override
    public UserDetailResponse getUserInfoByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UnauthorizedException(UNAUTHORIZED));
        return UserDetailResponse.fromEntity(user);
    }

    @Override
    public Boolean isEmailDuplicated(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public Boolean isNicknameDuplicated(String nickname) {
        return userRepository.existsByEmail(nickname);
    }
}
