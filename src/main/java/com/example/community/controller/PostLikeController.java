package com.example.community.controller;

import com.example.community.common.annotation.LoginUser;
import com.example.community.common.response.APIResponse;
import com.example.community.domain.User;
import com.example.community.dto.response.post.PostLikeResponse;
import com.example.community.service.post.like.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostLikeController {

    private final PostLikeService postLikeService;

    @PostMapping("/{postId}/likes")
    public ResponseEntity<APIResponse<PostLikeResponse>> addLike(
            @PathVariable Long postId,
            @LoginUser User user
    ) {
        PostLikeResponse postLikeResponse = postLikeService.addLike(postId, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(APIResponse.success("좋아요 생성 성공", postLikeResponse));
    }

    @DeleteMapping("/{postId}/likes")
    public ResponseEntity<APIResponse<PostLikeResponse>> removeLike(
            @PathVariable Long postId,
            @LoginUser User user
    ) {
        PostLikeResponse postLikeResponse = postLikeService.removeLike(postId, user);
        return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success("좋아요 취소 성공", postLikeResponse));
    }

    @GetMapping("/{postId}/likes")
    public ResponseEntity<APIResponse<PostLikeResponse>> getLikeCount(
            @PathVariable Long postId,
            @LoginUser User user
    ) {
        PostLikeResponse postLikeResponse = postLikeService.getLikeCount(postId, user);
        return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success("좋아요 조회 성공", postLikeResponse));
    }

}
