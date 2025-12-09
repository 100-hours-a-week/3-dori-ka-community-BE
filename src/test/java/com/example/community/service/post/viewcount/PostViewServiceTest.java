package com.example.community.service.post.viewcount;

import com.example.community.common.exception.custom.ResourceNotFoundException;
import com.example.community.domain.Post;
import com.example.community.domain.User;
import com.example.community.repository.post.PostJdbcRepository;
import com.example.community.repository.post.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostViewServiceTest {

    private static final long POST_ID = 1L;
    private static final long INITIAL_VIEW_COUNT = 3L;
    private static final long USER_ID = 100L;

    @Mock private PostRepository postRepository;
    @Mock private PostJdbcRepository postJdbcRepository;
    @Mock private CacheManager cacheManager;
    @Mock private Cache cache;

    @InjectMocks
    private PostViewServiceImpl postViewService;

    @Test
    @DisplayName("조회수 증가 - 캐시에 값이 존재하는 경우")
    void increaseViewcount_success_cache_present() {
        Post post = createPost(POST_ID, INITIAL_VIEW_COUNT);

        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));
        when(cacheManager.getCache("viewcount")).thenReturn(cache);
        when(cache.get(POST_ID, Long.class)).thenReturn(5L);

        Long result = postViewService.increaseViewcount(POST_ID);

        assertThat(result).isEqualTo(6L);
        verify(cache).put(POST_ID, 6L);
    }

    @Test
    @DisplayName("조회수 증가 - 캐시에 값이 없는 경우(Post 값 사용)")
    void increaseViewcount_success_cache_empty() {
        Post post = createPost(POST_ID, INITIAL_VIEW_COUNT);

        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));
        when(cacheManager.getCache("viewcount")).thenReturn(cache);
        when(cache.get(POST_ID, Long.class)).thenReturn(null);

        Long result = postViewService.increaseViewcount(POST_ID);

        assertThat(result).isEqualTo(INITIAL_VIEW_COUNT + 1);
        verify(cache).put(POST_ID, INITIAL_VIEW_COUNT + 1);
    }

    @Test
    @DisplayName("조회수 증가 - 캐시 매니저 없음")
    void increaseViewcount_success_no_cache_manager() {
        Post post = createPost(POST_ID, INITIAL_VIEW_COUNT);

        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));
        when(cacheManager.getCache("viewcount")).thenReturn(null);

        Long result = postViewService.increaseViewcount(POST_ID);

        assertThat(result).isEqualTo(INITIAL_VIEW_COUNT + 1);
        verifyNoInteractions(cache);
    }

    @Test
    @DisplayName("조회수 증가 - 게시글 없음")
    void increaseViewcount_fail_post_not_found() {
        when(postRepository.findById(POST_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postViewService.increaseViewcount(POST_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("조회수 조회 - 캐시에 값이 있을 경우")
    void get_viewcount_success_cache_present() {
        when(cacheManager.getCache("viewcount")).thenReturn(cache);
        when(cache.get(POST_ID, Long.class)).thenReturn(8L);

        Long result = postViewService.getViewCount(POST_ID);

        assertThat(result).isEqualTo(8L);
        verify(postRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("조회수 조회 - 캐시에 값이 없을 경우")
    void get_viewcount_success_cache_miss() {
        Post post = createPost(POST_ID, INITIAL_VIEW_COUNT);

        when(cacheManager.getCache("viewcount")).thenReturn(cache);
        when(cache.get(POST_ID, Long.class)).thenReturn(null);
        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));

        Long result = postViewService.getViewCount(POST_ID);

        assertThat(result).isEqualTo(INITIAL_VIEW_COUNT);
    }

    @Test
    @DisplayName("조회수 조회 - 캐시 미사용")
    void get_viewcount_success_no_cache() {
        Post post = createPost(POST_ID, INITIAL_VIEW_COUNT);

        when(cacheManager.getCache("viewcount")).thenReturn(null);
        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));

        Long result = postViewService.getViewCount(POST_ID);

        assertThat(result).isEqualTo(INITIAL_VIEW_COUNT);
    }

    @Test
    @DisplayName("조회수 조회 - 게시글 없음")
    void get_viewcount_fail_not_found() {
        when(cacheManager.getCache("viewcount")).thenReturn(cache);
        when(cache.get(POST_ID, Long.class)).thenReturn(null);
        when(postRepository.findById(POST_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postViewService.getViewCount(POST_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("조회수 동기화 - 캐시 없음")
    void sync_viewcount_success_no_cache() {
        when(cacheManager.getCache("viewcount")).thenReturn(null);

        postViewService.syncViewCount();

        verifyNoInteractions(postRepository);
    }

    @Test
    @DisplayName("조회수 동기화 - 캐시 값 DB 반영")
    void sync_viewcount_success_update_to_post() {
        Post post = createPost(POST_ID, INITIAL_VIEW_COUNT);
        ConcurrentHashMap<Long, Long> cacheStore = new ConcurrentHashMap<>();
        cacheStore.put(POST_ID, 20L);

        when(cacheManager.getCache("viewcount")).thenReturn(cache);
        when(cache.getNativeCache()).thenReturn(cacheStore);
        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));

        postViewService.syncViewCount();

        assertThat(post.getViewCount()).isEqualTo(20L);
        verify(postRepository).findById(POST_ID);
    }

    @Test
    @DisplayName("조회수 벌크 동기화 - 캐시 없음")
    void sync_viewcount_bulk_success_no_cache() {
        when(cacheManager.getCache("viewcount")).thenReturn(null);

        postViewService.syncViewCountBulk();

        verifyNoInteractions(postJdbcRepository);
    }

    @Test
    @DisplayName("조회수 벌크 동기화 - 정상 업데이트 후 캐시 비우기")
    void sync_viewcount_bulk_success_clear_cache() {
        ConcurrentHashMap<Long, Long> cacheStore = new ConcurrentHashMap<>();
        cacheStore.put(POST_ID, 30L);
        cacheStore.put(2L, 40L);

        when(cacheManager.getCache("viewcount")).thenReturn(cache);
        when(cache.getNativeCache()).thenReturn(cacheStore);

        postViewService.syncViewCountBulk();

        ArgumentCaptor<Map<Long, Long>> captor = ArgumentCaptor.forClass(Map.class);
        verify(postJdbcRepository).bulkUpdateViewcounts(captor.capture());

        assertThat(captor.getValue())
                .containsEntry(POST_ID, 30L)
                .containsEntry(2L, 40L);

        assertThat(cacheStore).isEmpty();
    }

    @Test
    @DisplayName("조회수 벌크 동기화 - 실패 시 캐시 유지")
    void sync_viewcount_bulk_fail_keep_cache() {
        ConcurrentHashMap<Long, Long> cacheStore = new ConcurrentHashMap<>();
        cacheStore.put(POST_ID, 50L);

        when(cacheManager.getCache("viewcount")).thenReturn(cache);
        when(cache.getNativeCache()).thenReturn(cacheStore);
        doThrow(new RuntimeException("fail"))
                .when(postJdbcRepository).bulkUpdateViewcounts(anyMap());

        postViewService.syncViewCountBulk();

        assertThat(cacheStore).hasSize(1);
    }

    private Post createPost(Long id, Long viewCount) {
        Post post = Post.builder()
                .title("title")
                .content("content")
                .user(createUser())
                .build();
        ReflectionTestUtils.setField(post, "id", id);
        post.upViewcount(viewCount);
        return post;
    }

    private User createUser() {
        User user = User.builder()
                .email("user@test.com")
                .password("password")
                .nickname("tester")
                .profileImage("profile")
                .build();
        ReflectionTestUtils.setField(user, "id", USER_ID);
        return user;
    }
}