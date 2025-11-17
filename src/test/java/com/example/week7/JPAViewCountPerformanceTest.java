package com.example.week7;

import com.example.week7.domain.Post;
import com.example.week7.repository.post.PostRepository;
import com.example.week7.service.post.PostViewServiceImpl;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@DisplayName("[JPA] Dirty Checking 방식 10,000건 업데이트 성능 테스트")
public class JPAViewCountPerformanceTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostViewServiceImpl postViewService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private EntityManager em;

    private List<Long> postIds;

    @BeforeEach
    void setup() {
        postIds = new ArrayList<>();

        // 🔥 10,000개 게시글 생성
        for (int i = 0; i < 10_000; i++) {
            Post saved = postRepository.save(
                    Post.builder()
                            .title("title")
                            .content("content")
                            .user(null)
                            .build()
            );
            postIds.add(saved.getId());
        }
    }

    @Test
    @Transactional
    @DisplayName("JPA 10,000건 Dirty Checking 성능 테스트")
    void performance_JPA_10000_Update() {
        Cache cache = cacheManager.getCache("viewcount");

        // 캐시에 10,000개 값 넣기
        for (Long id : postIds) {
            cache.put(id, 100L);
        }

        long start = System.currentTimeMillis();

        // Dirty checking + flush
        postViewService.syncViewCount();
        em.flush();
        em.clear();

        long end = System.currentTimeMillis();

        System.out.println("\n====================== JPA 10,000 update ======================");
        System.out.println("JPA Dirty Checking → 10,000건 UPDATE: " + (end - start) + "ms");
        System.out.println("===============================================================\n");
    }
}