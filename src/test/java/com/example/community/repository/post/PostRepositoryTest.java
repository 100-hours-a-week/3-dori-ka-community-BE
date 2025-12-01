package com.example.community.repository.post;

import com.example.community.domain.Post;
import com.example.community.domain.User;
import com.example.community.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class PostRepositoryTest {

    private static final String TITLE = "test title";
    private static final String CONTENT = "test content";
    private static final Long UNKNOWN_POST_ID = 10011L;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void init() {
        user = User.builder()
                .email("test@test.com")
                .password("1234")
                .nickname("test")
                .profileImage("profileImage")
                .build();

        user = userRepository.save(user);
    }

    @Test
    @DisplayName("게시글 저장 - 성공")
    void save_post_success() {
        Post post = createPost(TITLE, CONTENT);

        //when
        Post savedPost = postRepository.save(post);
        Optional<Post> findPost = postRepository.findById(savedPost.getId());

        //then
        assertThat(findPost)
                .hasValueSatisfying(found -> {
                    assertThat(found.getTitle()).isEqualTo(TITLE);
                    assertThat(found.getContent()).isEqualTo(CONTENT);
                    assertThat(found.getUser().getEmail()).isEqualTo(user.getEmail());
                });
    }

    @Test
    @DisplayName("게시글 저장 - 실패")
    void save_post_fail() {
        //when & then
        assertThatThrownBy(() -> postRepository.saveAndFlush(createPost(null, CONTENT)))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> postRepository.saveAndFlush(createPost(TITLE, null)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("게시글 조회")
    void find_post() {
        Post savedPost = postRepository.save(createPost(TITLE, CONTENT));

        //when
        Optional<Post> findPost = postRepository.findById(savedPost.getId());
        Optional<Post> findWrongPost = postRepository.findById(UNKNOWN_POST_ID);

        //then
        assertThat(findPost)
                .hasValueSatisfying(found -> {
                    assertThat(found.getTitle()).isEqualTo(TITLE);
                    assertThat(found.getContent()).isEqualTo(CONTENT);
                });
        assertThat(findWrongPost).isEmpty();
    }

    @Test
    @DisplayName("게시글 조회 - 사용자별")
    void find_post_user() {
        postRepository.saveAll(List.of(
                createPost(TITLE, CONTENT),
                createPost(TITLE + "1", CONTENT + "1"),
                createPost(TITLE + "2", CONTENT + "2")
        ));

        //when
        List<Post> posts = postRepository.findAllByUser(user);

        //then
        assertThat(posts)
                .hasSize(3)
                .allMatch(post -> post.getUser().equals(user));

    }

    private Post createPost(String title, String content) {
        return Post.builder()
                .title(title)
                .content(content)
                .user(user)
                .build();
    }
}
