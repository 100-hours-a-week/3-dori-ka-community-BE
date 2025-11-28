package com.example.community.service.post.like;

import com.example.community.domain.User;
import com.example.community.dto.response.post.PostLikeResponse;

public interface PostLikeService {

    PostLikeResponse addLike(Long postId, User user);

    PostLikeResponse removeLike(Long postId, User user);

    PostLikeResponse getLikeCount(Long postId, User usr);

}
