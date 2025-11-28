package com.example.community.dto.response.post;

import com.example.community.domain.PostLike;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PostLikeResponse {

    private Long postId;
    private Boolean liked;
    private Long likeCount;

    @Builder
    public PostLikeResponse(Long postId, boolean liked, long likeCount) {
        this.postId = postId;
        this.liked = liked;
        this.likeCount = likeCount;
    }

    public static PostLikeResponse fromEntity(PostLike postLike, Boolean liked, long likeCount) {
        return PostLikeResponse.builder()
                .postId(postLike.getPost().getId())
                .liked(liked)
                .likeCount(likeCount)
                .build();
    }
}
