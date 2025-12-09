package com.example.community.service.user;

import com.example.community.common.exception.ErrorMessage;
import com.example.community.common.exception.custom.BadRequestException;
import com.example.community.common.exception.custom.DuplicatedException;
import com.example.community.common.exception.custom.ResourceNotFoundException;
import com.example.community.common.util.AuthValidator;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.example.community.common.exception.ErrorMessage.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

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

        doThrow(new DuplicatedException(EMAIL_DUPLICATED))
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

        doThrow(new BadRequestException(PASSWORD_MISMATCH))
                .when(authValidator).checkPassword(dto.getPassword(), dto.getPasswordCheck());

        assertThatThrownBy(() -> userService.signUp(dto))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("사용자 정보 수정 - 성공")
    void user_update_success() {
        User user = createUser("test@test.com", "origin", "old.png");
        User persisted = createUser("test@test.com", "origin", "old.png");
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
    @DisplayName("사용자 정보 수정 - 실패(닉네임 중복)")
    void user_update_fail_nickname_duplicate() {
        User user = createUser("test@test.com", "origin", "old.png");
        User persisted = createUser("test@test.com", "origin", "old.png");
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
    @DisplayName("사용자 정보 수정 - 실패(존재하지 않는 사용자)")
    void user_update_fail_no_user() {
        User user = createUser("test@test.com", "origin", "old.png");

        UserUpdateDto dto = UserUpdateDto.builder()
                .nickname("other")
                .profileImage("new.png")
                .build();

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(dto, user))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("비밀번호 수정 - 성공")
    void change_password_success() {
        User user = createUser("test@test.com", "origin", "profile");
        User stored = createUser("test@test.com", "origin", "profile");
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
    @DisplayName("비밀번호 수정 - 실패(비밀번호 불일치)")
    void change_password_fail_mismatch_password() {
        User user = createUser("test@test.com", "origin", "profile");
        User stored = createUser("test@test.com", "origin", "profile");
        ChangePasswordDto dto = ChangePasswordDto.builder()
                .password("newPass1!")
                .passwordCheck("diffPass!")
                .build();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(stored));
        doThrow(new BadRequestException(PASSWORD_MISMATCH))
                .when(authValidator).checkPassword(dto.getPassword(), dto.getPasswordCheck());

        assertThatThrownBy(() -> userService.changePassword(dto, user))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("비밀번호 수정 - 실패(존재하지 않는 사용자")
    void change_password_fail_no_user() {
        User user = createUser("test@test.com", "origin", "profile");

        ChangePasswordDto dto = ChangePasswordDto.builder()
                .password("newPass1!")
                .passwordCheck("newPass1!")
                .build();

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.changePassword(dto, user))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(authValidator, never()).checkPassword(dto.getPassword(), dto.getPasswordCheck());
    }

    @Test
    @DisplayName("사용자 정보 조회 - success")
    void find_userInfo_success() {

        User user = createUser("test@test.com", "tester", "profile");

        UserDetailResponse userInfo = userService.getUserInfo(user);

        assertThat(userInfo.getEmail()).isEqualTo(user.getEmail());
        assertThat(userInfo.getNickname()).isEqualTo(user.getNickname());
    }

    @Test
    @DisplayName("사용자 정보 조회 - Id")
    void find_userInfo_by_id() {

        User user = createUser("test@test.com", "tester", "profile");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDetailResponse response = userService.getUserInfoById(1L);

        assertThat(response.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    @DisplayName("회원 탈퇴 - 성공")
    void delete_user_success() {
        User user = createUser("test@test.com", "tester", "profile");

        Post post = Post.builder()
                .title("test title")
                .content("test content")
                .user(user)
                .build();

        Comment comment = Comment.builder()
                .content("test comment")
                .post(post)
                .user(user)
                .build();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .refreshToken("test refresh token")
                .expirationDate(LocalDateTime.now())
                .build();

        when(postRepository.findAllByUser(user))
                .thenReturn(List.of(post));

        when(commentRepository.findAllByUser(user))
                .thenReturn(List.of(comment));

        when(refreshTokenRepository.findByUser(user))
                .thenReturn(Optional.of(refreshToken));

        userService.delete(user);

        verify(postRepository).findAllByUser(user);
        verify(commentRepository).findAllByUser(user);
        verify(refreshTokenRepository).findByUser(user);
        verify(refreshTokenRepository).delete(refreshToken);
        verify(userRepository).delete(user);
    }

    @Test
    @DisplayName("회원 탈퇴 - 실패(no refresh token)")
    void delete_user_fail_no_refresh_token() {
        User user = createUser("test@test.com", "tester", "profile");

        when(refreshTokenRepository.findByUser(user))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.delete(user))
                .isInstanceOf(BadRequestException.class);

        verify(postRepository).findAllByUser(user);
        verify(commentRepository).findAllByUser(user);
        verify(refreshTokenRepository).findByUser(user);
        verify(refreshTokenRepository, never()).delete(any());
        verify(userRepository, never()).delete(user);
    }

    @Test
    @DisplayName("사용자 삭제 - ID")
    void delete_by_id() {
        userService.delete(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("이메일 중복 조회 - 성공")
    void check_email_duplicate_success() {
        String email = "test@test.com";

        when(userRepository.existsByEmail(email))
                .thenReturn(true);

        assertThat(userService.isEmailDuplicated(email)).isTrue();

        verify(userRepository).existsByEmail(email);
    }

    @Test
    @DisplayName("닉네임 중복 조회 - 성공")
    void check_nickname_duplicate_success() {
        String nickname = "tester";

        when(userRepository.existsByNickname(nickname))
                .thenReturn(true);

        assertThat(userService.isNicknameDuplicated(nickname)).isTrue();

        verify(userRepository).existsByNickname(nickname);
    }

    private User createUser(String email, String nickname, String profileImage) {
        return User.builder()
                .email(email)
                .password("1234")
                .nickname(nickname)
                .profileImage(profileImage)
                .build();
    }
}
