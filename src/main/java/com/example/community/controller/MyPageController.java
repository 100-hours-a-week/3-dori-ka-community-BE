package com.example.community.controller;

import com.example.community.common.annotation.LoginUser;
import com.example.community.common.response.APIResponse;
import com.example.community.domain.User;
import com.example.community.dto.request.user.ChangePasswordDto;
import com.example.community.dto.request.user.UserUpdateDto;
import com.example.community.dto.response.comment.CommentResponse;
import com.example.community.dto.response.post.PostListResponse;
import com.example.community.dto.response.user.UserDetailResponse;
import com.example.community.service.comment.CommentService;
import com.example.community.service.post.PostService;
import com.example.community.service.user.UserService;
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

    private final UserService userService;
    private final PostService postService;
    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<APIResponse<UserDetailResponse>> getUserInfo(@LoginUser User user) {
        UserDetailResponse userResponse = userService.getUserInfo(user);
        return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success("사용자 조회 성공", userResponse));
    }

    @GetMapping("/posts")
    public ResponseEntity<APIResponse<Page<PostListResponse>>> getPostsByUser(@LoginUser User user,
                                                                        @PageableDefault(size = 3, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PostListResponse> posts = postService.getAllPostByUser(user, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success("사용자 게시글 조회 성공", posts));
    }

    @GetMapping("/comments")
    public ResponseEntity<APIResponse<Page<CommentResponse>>> getCommentsByUser(@LoginUser User user,
                                                                          @PageableDefault(size = 3, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<CommentResponse> comments = commentService.getCommentByUser(user, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success("사용자 댓글 조회 성공", comments));
    }

    @PatchMapping
    public ResponseEntity<APIResponse<UserDetailResponse>> userUpdate(@RequestBody UserUpdateDto dto,
                                                                      @LoginUser User user) {

        UserDetailResponse userResponse = userService.updateUser(dto, user);

        return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success("사용자 정보 업데이트 성공", userResponse));
    }

    @PatchMapping("/pwd")
    public ResponseEntity<APIResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordDto dto,
                                                            @LoginUser User user) {
        userService.changePassword(dto, user);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping
    public ResponseEntity<APIResponse<Void>> delete(@LoginUser User user) {
        userService.delete(user);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
