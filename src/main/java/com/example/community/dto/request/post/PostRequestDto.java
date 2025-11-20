package com.example.community.dto.request.post;

import com.example.community.domain.Post;
import com.example.community.domain.PostImage;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PostRequestDto {

    @NotBlank(message = "제목을 작성해주세요")
    private String title;

    @NotBlank(message = "내용을 작성해주새요")
    private String content;

    private List<String> postImageUrls;

    @Builder
    public PostRequestDto(String title, String content, List<String> postImageUrls) {
        this.title = title;
        this.content = content;
        this.postImageUrls = postImageUrls;
    }

    public static Post ofEntity(PostRequestDto dto) {
        return Post.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .build();
    }
}
