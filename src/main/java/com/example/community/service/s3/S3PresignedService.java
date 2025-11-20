package com.example.community.service.s3;

import com.example.community.dto.response.image.PresignedUrlResponse;

public interface S3PresignedService {

    PresignedUrlResponse createdPresignedUrl(String contentType);

}
