package com.example.community.repository.post;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
@RequiredArgsConstructor
public class PostJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public void bulkUpdateViewcounts(Map<Long, Long> viewcounts) {
        jdbcTemplate.batchUpdate(
                "UPDATE post SET view_count = ? WHERE post_id = ?",
                viewcounts.entrySet(),
                100,
                (ps, entry) -> {
                    ps.setLong(1, entry.getValue());
                    ps.setLong(2, entry.getKey());
                });
    }
}
