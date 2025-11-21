package com.example.community.dto.response.post;

import com.example.community.domain.PostImage;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PostImageResponse {

    private Long postImageId;
    private String postImageUrl;

    @Builder
    public PostImageResponse(Long postImageId, String postImageUrl) {
        this.postImageId = postImageId;
        this.postImageUrl = postImageUrl;
    }

    public static PostImageResponse fromEntity(PostImage postImage) {
        return PostImageResponse.builder()
                .postImageId(postImage.getId())
                .postImageUrl(postImage.getPostImageUrl())
                .build();
    }
}
