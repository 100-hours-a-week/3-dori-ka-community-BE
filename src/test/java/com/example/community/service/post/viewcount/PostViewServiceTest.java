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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostViewServiceTest {

    private static final long POST_ID = 1L;
    private static final long INITIAL_VIEW_COUNT = 3L;
    private static final long USER_ID = 100L;

    @Mock
    private PostRepository postRepository;
    @Mock
    private PostJdbcRepository postJdbcRepository;
    @Mock
    private CacheManager cacheManager;
    @Mock
    private Cache cache;

    @InjectMocks
    private PostViewServiceImpl postViewService;

    @DisplayName("increaseViewcount increments based on cached value when present")
    @Test
    void increaseViewcount_usesCachedValueWhenPresent() {
        Post post = createPost(POST_ID, INITIAL_VIEW_COUNT);
        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));
        when(cacheManager.getCache("viewcount")).thenReturn(cache);
        when(cache.get(POST_ID, Long.class)).thenReturn(5L);

        Long result = postViewService.increaseViewcount(POST_ID);

        assertThat(result).isEqualTo(6L);
        verify(cache).put(POST_ID, 6L);
    }

    @DisplayName("increaseViewcount uses post viewcount when cache entry is missing")
    @Test
    void increaseViewcount_initializesFromPostWhenCacheEmpty() {
        Post post = createPost(POST_ID, INITIAL_VIEW_COUNT);
        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));
        when(cacheManager.getCache("viewcount")).thenReturn(cache);
        when(cache.get(POST_ID, Long.class)).thenReturn(null);

        Long result = postViewService.increaseViewcount(POST_ID);

        assertThat(result).isEqualTo(INITIAL_VIEW_COUNT + 1);
        verify(cache).put(POST_ID, INITIAL_VIEW_COUNT + 1);
    }

    @DisplayName("increaseViewcount still works when cache manager returns null")
    @Test
    void increaseViewcount_handlesMissingCache() {
        Post post = createPost(POST_ID, INITIAL_VIEW_COUNT);
        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));
        when(cacheManager.getCache("viewcount")).thenReturn(null);

        Long result = postViewService.increaseViewcount(POST_ID);

        assertThat(result).isEqualTo(INITIAL_VIEW_COUNT + 1);
        verifyNoInteractions(cache);
    }

    @DisplayName("increaseViewcount throws when the post cannot be found")
    @Test
    void increaseViewcount_throwsWhenPostMissing() {
        when(postRepository.findById(POST_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postViewService.increaseViewcount(POST_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @DisplayName("getViewCount returns cached value without hitting the repository")
    @Test
    void getViewCount_returnsCachedValue() {
        when(cacheManager.getCache("viewcount")).thenReturn(cache);
        when(cache.get(POST_ID, Long.class)).thenReturn(8L);

        Long result = postViewService.getViewCount(POST_ID);

        assertThat(result).isEqualTo(8L);
        verify(postRepository, never()).findById(anyLong());
    }

    @DisplayName("getViewCount fetches from repository when cache misses")
    @Test
    void getViewCount_fetchesFromRepositoryWhenNotCached() {
        Post post = createPost(POST_ID, INITIAL_VIEW_COUNT);
        when(cacheManager.getCache("viewcount")).thenReturn(cache);
        when(cache.get(POST_ID, Long.class)).thenReturn(null);
        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));

        Long result = postViewService.getViewCount(POST_ID);

        assertThat(result).isEqualTo(INITIAL_VIEW_COUNT);
    }

    @DisplayName("getViewCount relies on repository when no cache is configured")
    @Test
    void getViewCount_handlesMissingCache() {
        Post post = createPost(POST_ID, INITIAL_VIEW_COUNT);
        when(cacheManager.getCache("viewcount")).thenReturn(null);
        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));

        Long result = postViewService.getViewCount(POST_ID);

        assertThat(result).isEqualTo(INITIAL_VIEW_COUNT);
    }

    @DisplayName("getViewCount throws when neither cache nor repository can provide the post")
    @Test
    void getViewCount_throwsWhenPostMissing() {
        when(cacheManager.getCache("viewcount")).thenReturn(cache);
        when(cache.get(POST_ID, Long.class)).thenReturn(null);
        when(postRepository.findById(POST_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postViewService.getViewCount(POST_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @DisplayName("syncViewCount exits immediately when cache is missing")
    @Test
    void syncViewCount_returnsWhenCacheMissing() {
        when(cacheManager.getCache("viewcount")).thenReturn(null);

        postViewService.syncViewCount();

        verifyNoInteractions(postRepository);
    }

    @DisplayName("syncViewCount writes cached viewcounts back into the posts")
    @Test
    void syncViewCount_updatesPostsFromCache() {
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

    @DisplayName("syncViewCountBulk exits when cache is missing")
    @Test
    void syncViewCountBulk_returnsWhenCacheMissing() {
        when(cacheManager.getCache("viewcount")).thenReturn(null);

        postViewService.syncViewCountBulk();

        verifyNoInteractions(postJdbcRepository);
    }

    @DisplayName("syncViewCountBulk sends cached entries to JDBC repository and clears the cache")
    @Test
    void syncViewCountBulk_updatesInBulkAndClearsCache() {
        ConcurrentHashMap<Long, Long> cacheStore = new ConcurrentHashMap<>();
        cacheStore.put(POST_ID, 30L);
        cacheStore.put(2L, 40L);
        when(cacheManager.getCache("viewcount")).thenReturn(cache);
        when(cache.getNativeCache()).thenReturn(cacheStore);

        postViewService.syncViewCountBulk();

        ArgumentCaptor<Map<Long, Long>> captor = ArgumentCaptor.forClass(Map.class);
        verify(postJdbcRepository).bulkUpdateViewcounts(captor.capture());
        assertThat(captor.getValue()).containsEntry(POST_ID, 30L).containsEntry(2L, 40L);
        assertThat(cacheStore).isEmpty();
    }

    @DisplayName("syncViewCountBulk keeps cache entries when bulk update fails")
    @Test
    void syncViewCountBulk_logsErrorAndKeepsCacheOnFailure() {
        ConcurrentHashMap<Long, Long> cacheStore = new ConcurrentHashMap<>();
        cacheStore.put(POST_ID, 50L);
        when(cacheManager.getCache("viewcount")).thenReturn(cache);
        when(cache.getNativeCache()).thenReturn(cacheStore);
        doThrow(new RuntimeException("fail")).when(postJdbcRepository).bulkUpdateViewcounts(anyMap());

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
