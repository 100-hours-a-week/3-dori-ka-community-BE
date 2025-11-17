package com.example.week7.controller;

import com.example.week7.common.annotation.AuthUser;
import com.example.week7.common.response.APIResponse;
import com.example.week7.dto.request.user.ChangePasswordDto;
import com.example.week7.dto.request.user.UserUpdateDto;
import com.example.week7.dto.response.comment.CommentResponse;
import com.example.week7.dto.response.post.PostListResponse;
import com.example.week7.dto.response.user.UserDetailResponse;
import com.example.week7.service.comment.CommentService;
import com.example.week7.service.post.PostService;
import com.example.week7.service.user.UserCommandService;
import com.example.week7.service.user.UserQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/me")
@RequiredArgsConstructor
public class MyPageController {

    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;
    private final PostService postService;
    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<APIResponse<UserDetailResponse>> getUserInfo(@AuthUser String email) {
        UserDetailResponse user = userQueryService.getUserInfoByEmail(email);
        return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success("사용자 조회 성공", user));
    }

    @GetMapping("/posts")
    public ResponseEntity<APIResponse<Page<PostListResponse>>> getPostsByUser(@AuthUser String email,
                                                                        @PageableDefault(size = 3, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PostListResponse> posts = postService.getAllPostByUser(email, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success("사용자 게시글 조회 성공", posts));
    }

    @GetMapping("/comments")
    public ResponseEntity<APIResponse<Page<CommentResponse>>> getCommentsByUser(@AuthUser String email,
                                                                          @PageableDefault(size = 3, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<CommentResponse> comments = commentService.getCommentByUser(email, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success("사용자 댓글 조회 성공", comments));
    }

    @PatchMapping
    public ResponseEntity<APIResponse<UserDetailResponse>> userUpdate(@RequestBody UserUpdateDto dto,
                                                                      @AuthUser String email) {

        UserDetailResponse user = userCommandService.updateUser(dto, email);

        return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success("사용자 정보 업데이트 성공", user));
    }

    @PatchMapping("/pwd")
    public ResponseEntity<APIResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordDto dto,
                                                            @AuthUser String email) {

        userCommandService.changePassword(dto, email);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping
    public ResponseEntity<APIResponse<Void>> delete(@AuthUser String email) {
        userCommandService.delete(email);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
