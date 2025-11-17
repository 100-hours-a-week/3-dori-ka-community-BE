package com.example.community.dto.response.comment;

import com.example.community.common.util.DateTimeUtils;
import com.example.community.domain.Comment;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentResponse {
    private Long commentId;
    private Long postId;
    private String content;
    private String writer;
    private String writerEmail;
    private String createdDate;
    private String modifiedDate;

    @Builder
    public CommentResponse(Long commentId, Long postId, String content, String writer, String writerEmail, String createdDate, String modifiedDate) {
        this.commentId = commentId;
        this.postId = postId;
        this.content = content;
        this.writer = writer;
        this.writerEmail = writerEmail;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
    }


    public static CommentResponse fromEntity(Comment comment) {
        return CommentResponse.builder()
                .commentId(comment.getId())
                .postId(comment.getPost().getId())
                .content(comment.getContent())
                .writer(comment.getUser().getNickname())
                .writerEmail(comment.getUser().getEmail())
                .createdDate(DateTimeUtils.format(comment.getCreatedDate()))
                .modifiedDate(DateTimeUtils.format(comment.getModifiedDate()))
                .build();
    }
}
