package com.example.community.controller;


import com.example.community.common.response.APIResponse;
import com.example.community.dto.response.post.PostImageResponse;
import com.example.community.service.post.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts/{id}/images")
public class PostImageController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<APIResponse<List<PostImageResponse>>> getPostImages(@PathVariable Long id) {
        List<PostImageResponse> postImages = postService.getAllPostImageByPostId(id);
        return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success("게시글 이미지 조회 성공", postImages));
    }

}
