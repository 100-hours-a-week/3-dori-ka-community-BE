package com.example.community.dto.request.image;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PresignedUrlRequestDto {

    private String contentType;

    public PresignedUrlRequestDto(String contentType) {
        this.contentType = contentType;
    }
}
