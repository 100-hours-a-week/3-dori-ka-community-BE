package com.example.community.service.s3;

import com.example.community.dto.response.image.PresignedUrlResponse;

public interface ProfileImageService {

    PresignedUrlResponse createdPresignedUrl(String prefix, String contentType);

}
