package com.example.community.service.post;

public interface PostViewService {

    Long increaseViewcount(Long postId);

    Long getViewCount(Long postId);

    void syncViewCount();
}
