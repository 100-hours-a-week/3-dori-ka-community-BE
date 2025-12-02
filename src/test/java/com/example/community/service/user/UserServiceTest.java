package com.example.community.service.user;

import com.example.community.common.exception.ErrorMessage;
import com.example.community.common.exception.custom.BadRequestException;
import com.example.community.common.exception.custom.DuplicatedException;
import com.example.community.common.util.AuthValidator;
import com.example.community.domain.User;
import com.example.community.dto.request.user.ChangePasswordDto;
import com.example.community.dto.request.user.UserSignUpDto;
import com.example.community.dto.request.user.UserUpdateDto;
import com.example.community.dto.response.user.SignUpResponse;
import com.example.community.dto.response.user.UserDetailResponse;
import com.example.community.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthValidator authValidator;
    @Mock
    private S3Client s3Client;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "bucket", "test-bucket");
    }

    @Test
    @DisplayName("회원가입 성공")
    void signup_success() {
        UserSignUpDto dto = UserSignUpDto.builder()
                .email("test@test.com")
                .password("1234")
                .passwordCheck("1234")
                .nickname("test")
                .profileImage("image")
                .build();

        when(passwordEncoder.encode("1234")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SignUpResponse response = userService.signUp(dto);

        verify(authValidator).isExistEmail(dto.getEmail());
        verify(authValidator).isExistNickname(dto.getNickname());
        verify(authValidator).checkPassword(dto.getPassword(), dto.getPasswordCheck());
        assertThat(response.getEmail()).isEqualTo(dto.getEmail());
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void signup_duplicate_email() {
        UserSignUpDto dto = UserSignUpDto.builder()
                .email("test@test.com")
                .password("1234")
                .passwordCheck("1234")
                .nickname("test")
                .profileImage("image")
                .build();

        doThrow(new DuplicatedException(ErrorMessage.EMAIL_DUPLICATED))
                .when(authValidator).isExistEmail(dto.getEmail());

        assertThatThrownBy(() -> userService.signUp(dto))
                .isInstanceOf(DuplicatedException.class);

        verify(authValidator, never()).checkPassword(anyString(), anyString());
    }

    @Test
    @DisplayName("회원가입 실패 - 비밀번호 불일치")
    void signup_password_different() {
        UserSignUpDto dto = UserSignUpDto.builder()
                .email("test@test.com")
                .password("1234")
                .passwordCheck("12345")
                .nickname("test1")
                .profileImage("profileImage")
                .build();

        doThrow(new BadRequestException(ErrorMessage.PASSWORD_MISMATCH))
                .when(authValidator).checkPassword(dto.getPassword(), dto.getPasswordCheck());

        assertThatThrownBy(() -> userService.signUp(dto))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("사용자 정보 수정 - 성공")
    void user_update_success() {
        User user = buildUser("test@test.com", "origin", "old.png");
        User persisted = buildUser("test@test.com", "origin", "old.png");
        UserUpdateDto dto = UserUpdateDto.builder()
                .nickname("updated")
                .profileImage("new.png")
                .build();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(persisted));
        when(userRepository.existsByNickname(dto.getNickname())).thenReturn(false);
        when(s3Client.deleteObject(any(DeleteObjectRequest.class))).thenReturn(DeleteObjectResponse.builder().build());

        UserDetailResponse response = userService.updateUser(dto, user);

        assertThat(response.getNickname()).isEqualTo("updated");
        assertThat(response.getProfileImage()).isEqualTo("new.png");
        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("사용자 정보 수정 - 실패 - 닉네임 중복")
    void user_update_nickname_duplicate() {
        User user = buildUser("test@test.com", "origin", "old.png");
        User persisted = buildUser("test@test.com", "origin", "old.png");
        UserUpdateDto dto = UserUpdateDto.builder()
                .nickname("other")
                .profileImage("new.png")
                .build();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(persisted));
        when(userRepository.existsByNickname(dto.getNickname())).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(dto, user))
                .isInstanceOf(DuplicatedException.class);
    }

    @Test
    @DisplayName("비밀번호 수정 - 성공")
    void change_password_success() {
        User user = buildUser("test@test.com", "origin", "profile");
        User stored = buildUser("test@test.com", "origin", "profile");
        ChangePasswordDto dto = ChangePasswordDto.builder()
                .password("newPass1!")
                .passwordCheck("newPass1!")
                .build();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(stored));
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPw");

        userService.changePassword(dto, user);

        assertThat(stored.getPassword()).isEqualTo("encodedPw");
    }

    @Test
    @DisplayName("비밀번호 수정 - 실패")
    void change_password_fail() {
        User user = buildUser("test@test.com", "origin", "profile");
        User stored = buildUser("test@test.com", "origin", "profile");
        ChangePasswordDto dto = ChangePasswordDto.builder()
                .password("newPass1!")
                .passwordCheck("diffPass!")
                .build();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(stored));
        doThrow(new BadRequestException(ErrorMessage.PASSWORD_MISMATCH))
                .when(authValidator).checkPassword(dto.getPassword(), dto.getPasswordCheck());

        assertThatThrownBy(() -> userService.changePassword(dto, user))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("사용자 정보 조회 - success")
    void find_userInfo_success() {
        User user = buildUser("test@test.com", "tester", "profile");

        UserDetailResponse userInfo = userService.getUserInfo(user);

        assertThat(userInfo.getEmail()).isEqualTo(user.getEmail());
        assertThat(userInfo.getNickname()).isEqualTo(user.getNickname());
    }

    @Test
    @DisplayName("사용자 정보 조회 - Id")
    void find_userInfo_by_id() {

        User user = buildUser("test@test.com", "tester", "profile");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDetailResponse response = userService.getUserInfoById(1L);

        assertThat(response.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    @DisplayName("사용자 삭제 - ID")
    void delete_by_id() {
        userService.delete(1L);
        verify(userRepository).deleteById(1L);
    }

    private User buildUser(String email, String nickname, String profileImage) {
        return User.builder()
                .email(email)
                .password("1234")
                .nickname(nickname)
                .profileImage(profileImage)
                .build();
    }
}
