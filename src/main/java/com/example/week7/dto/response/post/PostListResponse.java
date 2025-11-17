package com.example.week7.dto.response.post;

import com.example.week7.common.util.DateTimeUtils;
import com.example.week7.domain.Post;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostListResponse {

    private Long postId;
    private String title;
    private String content;
    private String writer;
    private Long viewCount;
    private String createdDate;


    @Builder
    public PostListResponse(Long postId, String title, String content, String writer, Long viewCount, String createdDate) {
        this.postId = postId;
        this.title = title;
        this.content = content;
        this.writer = writer;
        this.viewCount = viewCount;
        this.createdDate = createdDate;
    }


    public static PostListResponse fromEntity(Post post) {
        return PostListResponse.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .writer(post.getUser().getNickname())
                .viewCount(post.getViewCount())
                .createdDate(DateTimeUtils.format(post.getCreatedDate()))
                .build();
    }
}
