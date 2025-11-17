package com.example.week7;

import com.example.week7.domain.Post;
import com.example.week7.repository.post.PostJdbcRepository;
import com.example.week7.repository.post.PostRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@DisplayName("[JDBC] Batch Update 방식 10,000건 업데이트 성능 테스트")
public class JDBCViewCountPerformanceTest {

    @Autowired
    private PostJdbcRepository postJdbcRepository;

    @Autowired
    private PostRepository postRepository;

    private Map<Long, Long> updateMap;

    @BeforeEach
    void setup() {
        updateMap = new HashMap<>();

        // 🔥 10,000개 게시글 생성
        for (int i = 0; i < 10_000; i++) {
            Post saved = postRepository.save(
                    Post.builder()
                            .title("title")
                            .content("content")
                            .user(null)
                            .build()
            );
            updateMap.put(saved.getId(), 100L);
        }
    }

    @Test
    @Transactional
    @DisplayName("JDBC Batch Update 10,000건 성능 테스트")
    void performance_JDBC_10000_BatchUpdate() {

        long start = System.currentTimeMillis();

        // 🔥 JDBC Batch update 수행
        postJdbcRepository.bulkUpdateViewcounts(updateMap);

        long end = System.currentTimeMillis();

        System.out.println("\n====================== JDBC 10,000 batch update ======================");
        System.out.println("JDBC Batch Update → 10,000건 UPDATE: " + (end - start) + "ms");
        System.out.println("========================================================================\n");
    }
}