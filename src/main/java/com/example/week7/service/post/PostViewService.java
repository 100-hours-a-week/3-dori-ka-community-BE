package com.example.week7.service.post;

public interface PostViewService {

    Long increaseViewcount(Long postId);

    Long getViewCount(Long postId);

    void syncViewCount();
}
