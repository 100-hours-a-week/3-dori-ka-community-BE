package com.example.community.service.post.viewcount;

public interface PostViewService {

    Long increaseViewcount(Long postId);

    Long getViewCount(Long postId);

    void syncViewCount();

    void syncViewCountBulk();
}
