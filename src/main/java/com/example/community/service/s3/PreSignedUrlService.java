package com.example.community.service.s3;

import com.example.community.dto.response.s3.PresignedUrlResponse;

public interface PreSignedUrlService {

    PresignedUrlResponse createdPresignedUrl(String prefix, String contentType);

}
