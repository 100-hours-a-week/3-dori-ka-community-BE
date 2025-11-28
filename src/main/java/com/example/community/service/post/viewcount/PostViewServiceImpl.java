package com.example.community.service.post.viewcount;

import com.example.community.common.exception.custom.ResourceNotFoundException;
import com.example.community.domain.Post;
import com.example.community.repository.post.PostJdbcRepository;
import com.example.community.repository.post.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import static com.example.community.common.exception.ErrorMessage.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostViewServiceImpl implements PostViewService {

    private final PostRepository postRepository;
    private final PostJdbcRepository postJdbcRepository;
    private final CacheManager cacheManager;


    @Transactional
    @CachePut(value = "viewcount", key = "#postId")
    public Long increaseViewcount(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new ResourceNotFoundException(RESOURCE_NOT_FOUND)
        );

        Cache cache = cacheManager.getCache("viewcount");
        Long current = cache != null ? cache.get(postId, Long.class) : null;

        if (current == null) {
            current = post.getViewCount();
        }

        Long updated = current + 1L;

        if (cache != null) {
            cache.put(postId, updated);
        }

        log.info("postId: {}, viewCount: {}", postId, updated);

        return updated;
    }

    @Cacheable(value = "viewcount", key = "#postId")
    public Long getViewCount(Long postId) {
        Cache cache = cacheManager.getCache("viewcount");
        Long viewcount = cache != null ? cache.get(postId, Long.class) : null;

        if (viewcount != null) {
            return viewcount;
        }

        Post post = postRepository.findById(postId).orElseThrow(
                () -> new ResourceNotFoundException(RESOURCE_NOT_FOUND)
        );

        return post.getViewCount();
    }

    @Override
    @Transactional(readOnly = false)
//    @Scheduled(fixedRate = 60000)
    public void syncViewCount() {
        Cache cache = cacheManager.getCache("viewcount");
        if (cache == null) return;

        Object nativeCache = cache.getNativeCache();
        if (nativeCache instanceof ConcurrentMap<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Long key = (Long) entry.getKey();
                Long viewcount = (Long) entry.getValue();
                log.info("key : {}, viewcount : {}", key, viewcount);
                postRepository.findById(key).ifPresent(post -> post.upViewcount(viewcount));
            }
        }
    }

    @Override
    @Transactional(readOnly = false)
    @Scheduled(fixedRate = 60000)
    public void syncViewCountBulk() {

        Cache cache = cacheManager.getCache("viewcount");
        if (cache == null) return;

        Object nativeCache = cache.getNativeCache();
        if (nativeCache instanceof ConcurrentMap<?, ?> map) {
            Map<Long, Long> updateMap = new HashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Long key = (Long) entry.getKey();
                Long viewcount = (Long) entry.getValue();
                updateMap.put(key, viewcount);
            }
            if (!updateMap.isEmpty()) {
                try {
                    postJdbcRepository.bulkUpdateViewcounts(updateMap);

                    map.clear();
                } catch (Exception e) {
                    log.error("조회수 동기화 실패");
                }
            }
        }
    }
}


