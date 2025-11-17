package com.example.week7.controller;

import com.example.week7.common.response.APIResponse;
import com.example.week7.dto.request.user.UserSignUpDto;
import com.example.week7.dto.response.user.SignUpResponse;
import com.example.week7.dto.response.user.UserDetailResponse;
import com.example.week7.service.user.UserCommandService;
import com.example.week7.service.user.UserQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;

    @PostMapping
    public ResponseEntity<APIResponse<SignUpResponse>> register(@Valid @RequestBody UserSignUpDto dto) {
        SignUpResponse registerMember = userCommandService.signUp(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(APIResponse.success("회원가입 성공", registerMember));
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<UserDetailResponse>> getUserInfo(@PathVariable Long id) {
        UserDetailResponse userInfo = userQueryService.getUserInfo(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(APIResponse.success("조회 성공", userInfo));
    }

    @GetMapping("/email")
    public ResponseEntity<APIResponse<Boolean>> isEmailDuplicated(@RequestParam String email) {
        Boolean emailDuplicated = userQueryService.isEmailDuplicated(email);
        return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success("이메일 중복 체크 성공", emailDuplicated));
    }


    @GetMapping("/nickname")
    public ResponseEntity<APIResponse<Boolean>> isNicknameDuplicated(@RequestParam String nickname) {
        Boolean nicknameDuplicated = userQueryService.isNicknameDuplicated(nickname);
        return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success("이메일 중복 체크 성공", nicknameDuplicated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userCommandService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
