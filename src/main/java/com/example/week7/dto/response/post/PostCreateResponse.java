package com.example.week7.dto.response.post;

import com.example.week7.common.util.DateTimeUtils;
import com.example.week7.domain.Post;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostCreateResponse {
    private Long postId;
    private String title;
    private String writer;
    private String createdDate;

    @Builder
    public PostCreateResponse(Long postId, String title, String writer, String createdDate) {
        this.postId = postId;
        this.title = title;
        this.writer = writer;
        this.createdDate = createdDate;
    }

    public static PostCreateResponse fromEntity(Post post) {
        return PostCreateResponse.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .writer(post.getUser().getNickname())
                .createdDate(DateTimeUtils.format(post.getCreatedDate()))
                .build();
    }
}
