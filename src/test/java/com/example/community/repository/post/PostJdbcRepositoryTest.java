package com.example.community.repository.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostJdbcRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private PostJdbcRepository postJdbcRepository;

    @Test
    @DisplayName("조회수 벌크 업데이트 - 성공")
    void bulk_update_viewcount_success() {

        Map<Long, Long> viewcounts = Map.of(
                1L, 10L,
                2L, 20L
        );

        postJdbcRepository.bulkUpdateViewcounts(viewcounts);

        verify(jdbcTemplate, times(1))
                .batchUpdate(
                        eq("UPDATE post SET view_count = ? WHERE post_id = ?"),
                        eq(viewcounts.entrySet()),
                        eq(100),
                        any()
                );
    }
    @Test
    @DisplayName("조회수 벌크 업데이트 - PreparedStatementSetter 검증")
    void bulk_update_viewcounts_preparedStatementSetter() throws Exception {
        // given
        Map<Long, Long> viewcounts = Map.of(1L, 10L);

        ArgumentCaptor<org.springframework.jdbc.core.ParameterizedPreparedStatementSetter<Map.Entry<Long, Long>>> setterCaptor =
                ArgumentCaptor.forClass(org.springframework.jdbc.core.ParameterizedPreparedStatementSetter.class);

        when(jdbcTemplate.batchUpdate(
                eq("UPDATE post SET view_count = ? WHERE post_id = ?"),
                eq(viewcounts.entrySet()),
                eq(100),
                setterCaptor.capture()
        )).thenReturn(new int[][]{{1}});

        postJdbcRepository.bulkUpdateViewcounts(viewcounts);

        var setter = setterCaptor.getValue();
        assertThat(setter).isNotNull();

        PreparedStatement ps = mock(PreparedStatement.class);

        Map.Entry<Long, Long> entry = Map.entry(1L, 10L);
        setter.setValues(ps, entry);

        verify(ps).setLong(1, 10L); // entry.getValue() → index 1
        verify(ps).setLong(2, 1L);  // entry.getKey() → index 2
    }
}