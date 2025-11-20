package com.example.community.controller;

import com.example.community.common.annotation.LoginUser;
import com.example.community.common.response.APIResponse;
import com.example.community.domain.User;
import com.example.community.dto.request.comment.CommentRequestDto;
import com.example.community.dto.response.comment.CommentResponse;
import com.example.community.service.comment.CommentServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentServiceImpl commentService;

    @PostMapping
    public ResponseEntity<APIResponse<CommentResponse>> createComment(
            @PathVariable Long postId,
            @RequestBody CommentRequestDto dto,
            @LoginUser User user
            ) {
        CommentResponse comment = commentService.createComment(dto, postId, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(APIResponse.success("댓글 작성 성공", comment));
    }

    @GetMapping
    public ResponseEntity<APIResponse<Page<CommentResponse>>> getComments(
            @PathVariable Long postId,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable

    ) {
        Page<CommentResponse> comments = commentService.getCommentByPost(postId, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success("댓글 조회 성공", comments));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<APIResponse<CommentResponse>> updateComment(
            @PathVariable Long postId,
            @PathVariable Long id,
            @RequestBody CommentRequestDto dto,
            @LoginUser User user
    ) {
        CommentResponse comment = commentService.update(dto, id, user);

        return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success("댓글 수정 성공", comment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<Void>> deleteComment(@PathVariable Long postId, @PathVariable Long id) {
        commentService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
