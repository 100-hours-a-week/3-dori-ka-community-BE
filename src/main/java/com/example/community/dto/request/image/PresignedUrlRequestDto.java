package com.example.community.dto.request.image;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PresignedUrlRequestDto {

    private String prefix;
    private String contentType;

    public PresignedUrlRequestDto(String prefix, String contentType) {
        this.prefix = prefix;
        this.contentType = contentType;
    }
}
