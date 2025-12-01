package com.example.community.repository.user;


import com.example.community.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private static final String DEFAULT_PASSWORD = "1234";
    private static final String DEFAULT_PROFILE_IMAGE = "profileImage";

    @Test
    @DisplayName("유저 저장 성공")
    void save_success() {
        //given
        User user = buildUser("test1@test.co.kr", "test1");

        //when
        userRepository.save(user);
        Optional<User> findUser = userRepository.findById(user.getId());

        //then
        assertThat(findUser).isPresent();
        assertThat(findUser.get().getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    @DisplayName("유저 저장 실패 - 이메일 중복, 닉네임 중복")
    void save_fail_duplicate_email() {

        //given
        User user1 = buildUser("test1@test.co.kr", "test1");
        User user2 = buildUser("test1@test.co.kr", "test2");
        User user3 = buildUser("test2@test.co.kr", "test1");

        //when
        userRepository.save(user1);

        //then
        assertThatThrownBy(() -> userRepository.saveAndFlush(user2)).isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> userRepository.saveAndFlush(user3)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("유저 이메일 조회")
    void find_Email() {

        //given
        User user1 = buildUser("test1@test.co.kr", "test1");

        userRepository.save(user1);

        //when
        Optional<User> findUser = userRepository.findByEmail("test1@test.co.kr");
        Optional<User> findWrongUser = userRepository.findByEmail("wrongemail@test.co.kr");

        //then
        assertThat(findUser).isPresent();
        assertThat(findUser.get().getEmail()).isEqualTo("test1@test.co.kr");
        assertThat(findWrongUser).isNotPresent();
    }

    @Test
    @DisplayName("유저 닉네임 조회")
    void find_Nickname() {

        //given
        User user1 = buildUser("test1@test.co.kr", "test1");

        userRepository.save(user1);

        //when
        Optional<User> findUser = userRepository.findByNickname("test1");
        Optional<User> findWrongUser = userRepository.findByNickname("wrong_nicknamer");

        //then
        assertThat(findUser).isPresent();
        assertThat(findUser.get().getNickname()).isEqualTo("test1");
        assertThat(findWrongUser).isNotPresent();
    }

    @Test
    @DisplayName("유저 이메일 조회 - exist")
    void exist_Email() {

        //given
        User user1 = buildUser("test1@test.co.kr", "test1");

        userRepository.save(user1);

        //when
        boolean success = userRepository.existsByEmail("test1@test.co.kr");
        boolean fail = userRepository.existsByEmail("test3@test.co.kr");

        //then
        assertThat(success).isTrue();
        assertThat(fail).isFalse();
    }

    private User buildUser(String email, String nickname) {
        return User.builder()
                .email(email)
                .password(DEFAULT_PASSWORD)
                .nickname(nickname)
                .profileImage(DEFAULT_PROFILE_IMAGE)
                .build();
    }
}
