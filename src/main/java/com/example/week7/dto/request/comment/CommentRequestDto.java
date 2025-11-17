package com.example.week7.dto.request.comment;

import com.example.week7.domain.Comment;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentRequestDto {

    @NotBlank(message = "댓글을 작성해주세요")
    private String content;

    @Builder
    public CommentRequestDto(String content) {
        this.content = content;
    }

    public static Comment ofEntity(CommentRequestDto dto) {
        return Comment.builder()
                .content(dto.getContent())
                .build();
    }
}
