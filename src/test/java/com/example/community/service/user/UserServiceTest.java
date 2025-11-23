package com.example.community.service.user;

import com.example.community.common.exception.custom.BadRequestException;
import com.example.community.common.exception.custom.DuplicatedException;
import com.example.community.domain.User;
import com.example.community.dto.request.user.ChangePasswordDto;
import com.example.community.dto.request.user.UserSignUpDto;
import com.example.community.dto.request.user.UserUpdateDto;
import com.example.community.dto.response.user.SignUpResponse;
import com.example.community.dto.response.user.UserDetailResponse;
import com.example.community.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;



import static org.assertj.core.api.Assertions.*;


@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("회원가입 성공")
    void signup_success() {

        // given
        UserSignUpDto user = UserSignUpDto.builder()
                .email("test@test.com")
                .password("1234")
                .passwordCheck("1234")
                .nickname("test")
                .profileImage("profileImage")
                .build();

        // when
        SignUpResponse response = userService.signUp(user);

        // then
        assertThat(response.getEmail()).isEqualTo("test@test.com");
        User saved = userRepository.findByEmail("test@test.com").orElseThrow();
        assertThat(passwordEncoder.matches("1234", saved.getPassword())).isTrue();
    }

    @Test
    @DisplayName("회원가입 실패 - 중복")
    void signup_duplicate() {

        // given
        UserSignUpDto user1 = UserSignUpDto.builder()
                .email("test@test.com")
                .password("1234")
                .passwordCheck("1234")
                .nickname("test1")
                .profileImage("profileImage")
                .build();

        UserSignUpDto user2 = UserSignUpDto.builder()
                .email("test@test.com")
                .password("1234")
                .passwordCheck("1234")
                .nickname("test2")
                .profileImage("profileImage")
                .build();

        UserSignUpDto user3 = UserSignUpDto.builder()
                .email("test2@test.com")
                .password("1234")
                .passwordCheck("1234")
                .nickname("test1")
                .profileImage("profileImage")
                .build();

        //when
        userService.signUp(user1);

        // then
        assertThatThrownBy(() -> userService.signUp(user2))
                .isInstanceOf(DuplicatedException.class);

        assertThatThrownBy(() -> userService.signUp(user3))
                .isInstanceOf(DuplicatedException.class);
    }

    @Test
    @DisplayName("회원가입 실패 - 비밀번호 불일치")
    void signup_password_different() {
        //given
        UserSignUpDto user = UserSignUpDto.builder()
                .email("test@test.com")
                .password("1234")
                .passwordCheck("12345")
                .nickname("test1")
                .profileImage("profileImage")
                .build();

        //when & then
        assertThatThrownBy(() -> userService.signUp(user))
                .isInstanceOf(BadRequestException.class);

    }

    @Test
    @DisplayName("사용자 정보 수정 - 성공")
    void user_update_success() {

        //given
        User user = User.builder()
                .email("test@test.com")
                .password("1234")
                .nickname("test")
                .profileImage("")
                .build();

        User savedUser = userRepository.save(user);

        UserUpdateDto update = UserUpdateDto.builder()
                .nickname("update")
                .profileImage("update").build();

        //when
        userService.updateUser(update, savedUser);

        //then
        assertThat(savedUser.getNickname()).isEqualTo(update.getNickname());
        assertThat(savedUser.getProfileImage()).isEqualTo(update.getProfileImage());
    }

    @Test
    @DisplayName("사용자 정보 수정 - 실패 - 닉네임 중복")
    void user_update_nickname_duplicate() {

        //given
        User user = User.builder()
                .email("test@test.com")
                .password("1234")
                .nickname("test")
                .profileImage("")
                .build();

        User user1 = User.builder()
                .email("test1@test.com")
                .password("1234")
                .nickname("test1")
                .profileImage("")
                .build();

        userRepository.save(user);
        User savedUser = userRepository.save(user1);


        //when
        UserUpdateDto update = UserUpdateDto.builder()
                .nickname("test")
                .profileImage("update").build();

        //then
        assertThatThrownBy(() -> userService.updateUser(update, savedUser))
                .isInstanceOf(DuplicatedException.class);
    }

    @Test
    @DisplayName("비밀번호 수정 - 성공")
    void change_password_success() {

        //given
        User user = User.builder()
                .email("test@test.com")
                .password("1234")
                .nickname("test")
                .profileImage("")
                .build();

        User savedUser = userRepository.save(user);

        ChangePasswordDto change = ChangePasswordDto.builder()
                .password("12345")
                .passwordCheck("12345")
                .build();

        //when
        userService.changePassword(change, savedUser);

        //then
        assertThat(passwordEncoder.matches("12345", savedUser.getPassword())).isEqualTo(true);
    }

    @Test
    @DisplayName("비밀번호 수정 - 실패")
    void change_password_fail() {

        //given
        User user = User.builder()
                .email("test@test.com")
                .password("1234")
                .nickname("test")
                .profileImage("")
                .build();

        User savedUser = userRepository.save(user);

        // when
        ChangePasswordDto change = ChangePasswordDto.builder()
                .password("12345")
                .passwordCheck("123456")
                .build();

        //then
        assertThatThrownBy(() -> userService.changePassword(change, savedUser)).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("사용자 정보 조회 - success")
    void find_userInfo_success() {

        //given
        User user = User.builder()
                .email("test@test.com")
                .password("1234")
                .nickname("test")
                .profileImage("1")
                .build();

        User savedUser = userRepository.save(user);

        //when
        UserDetailResponse userInfo = userService.getUserInfo(user);

        //then
        assertThat(userInfo.getEmail()).isEqualTo(user.getEmail());
        assertThat(userInfo.getNickname()).isEqualTo(user.getNickname());
        assertThat(userInfo.getProfileImage()).isEqualTo(user.getProfileImage());
    }


}