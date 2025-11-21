package com.example.community.dto.response.image;

import lombok.Getter;

@Getter
public class PresignedUrlResponse {

    private String presignedUrl;
    private String key;
    private String profileImageUrl;

    public PresignedUrlResponse(String presignedUrl, String key, String profileImageUrl) {
        this.presignedUrl = presignedUrl;
        this.key = key;
        this.profileImageUrl = profileImageUrl;
    }

    public static PresignedUrlResponse of(String presignedUrl, String key, String profileImageUrl) {
        return new PresignedUrlResponse(presignedUrl, key, profileImageUrl);
    }
}
