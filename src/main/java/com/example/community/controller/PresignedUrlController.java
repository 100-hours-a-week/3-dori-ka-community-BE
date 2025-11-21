package com.example.community.controller;

import com.example.community.common.response.APIResponse;
import com.example.community.dto.request.image.PresignedUrlRequestDto;
import com.example.community.dto.response.s3.PresignedUrlResponse;
import com.example.community.service.s3.PreSignedUrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/presigned-url")
public class PresignedUrlController {

    private final PreSignedUrlService preSignedUrlService;

    @PostMapping
    public ResponseEntity<APIResponse<PresignedUrlResponse>> createPresignedUrl(@RequestBody PresignedUrlRequestDto dto) {
        PresignedUrlResponse response = preSignedUrlService.createdPresignedUrl(dto.getPrefix(), dto.getContentType());
        return ResponseEntity.status(HttpStatus.CREATED).body(APIResponse.success("url 생성 성공", response));
    }

}
