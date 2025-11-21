package com.example.community.dto.request.post;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PostUpdateDto {

    @NotBlank(message = "제목을 작성해주세요")
    private String title;

    @NotBlank(message = "내용을 작성해주새요")
    private String content;

    private List<Long> keptImageIds;
    private List<Long> deletedImageIds;
    private List<String> newPostImageUrls;

    @Builder
    public PostUpdateDto(String title, String content, List<Long> keptImageIds, List<Long> deletedImageIds, List<String> newPostImageUrls) {
        this.title = title;
        this.content = content;
        this.keptImageIds = keptImageIds;
        this.deletedImageIds = deletedImageIds;
        this.newPostImageUrls = newPostImageUrls;
    }
}
