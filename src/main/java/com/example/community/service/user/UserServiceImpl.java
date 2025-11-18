package com.example.community.service.user;

import com.example.community.common.util.AuthValidator;
import com.example.community.common.exception.custom.*;
import com.example.community.domain.Comment;
import com.example.community.domain.Post;
import com.example.community.domain.RefreshToken;
import com.example.community.domain.User;
import com.example.community.dto.request.user.ChangePasswordDto;
import com.example.community.dto.request.user.UserSignUpDto;
import com.example.community.dto.request.user.UserUpdateDto;
import com.example.community.dto.response.user.SignUpResponse;
import com.example.community.dto.response.user.UserDetailResponse;
import com.example.community.repository.comment.CommentRepository;
import com.example.community.repository.post.PostRepository;
import com.example.community.repository.token.RefreshTokenRepository;
import com.example.community.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.community.common.exception.ErrorMessage.*;


@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthValidator authValidator;

    @Override
    public SignUpResponse signUp(UserSignUpDto dto) {
        authValidator.isExistEmail(dto.getEmail());
        authValidator.isExistNickname(dto.getNickname());
        authValidator.checkPassword(dto.getPassword(), dto.getPasswordCheck());
        User user = UserSignUpDto.ofEntity(dto);

        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        user.changePassword(encodedPassword);
        User saveUser = userRepository.save(user);
        return SignUpResponse.fromEntity(saveUser);
    }

    @Override
    public UserDetailResponse getUserInfo(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(RESOURCE_NOT_FOUND));
        return UserDetailResponse.fromEntity(user);
    }

    @Override
    public UserDetailResponse updateUser(UserUpdateDto dto, User user) {

        user.update(dto.getNickname(), dto.getProfileImage());

        return UserDetailResponse.fromEntity(user);
    }

    @Override
    public void changePassword(ChangePasswordDto dto, User user) {

        authValidator.checkPassword(dto.getPassword(), dto.getPasswordCheck());
        user.changePassword(passwordEncoder.encode(dto.getPassword()));
    }

    @Override
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public void delete(User user) {
        List<Post> posts = postRepository.findAllByUser(user);
        posts.forEach(post -> post.setMappingUser(null));

        List<Comment> comments = commentRepository.findAllByUser(user);
        comments.forEach(comment -> comment.setMappingUser(null));

        RefreshToken refreshToken = refreshTokenRepository.findByUser(user).orElseThrow(
                () -> new BadRequestException(TOKEN_EXPIRE)
        );
        refreshTokenRepository.delete(refreshToken);
        userRepository.delete(user);
    }

    @Override
    public UserDetailResponse getUserInfoById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(RESOURCE_NOT_FOUND));
        return UserDetailResponse.fromEntity(user);
    }

    @Override
    public UserDetailResponse getUserInfo(User user) {
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
