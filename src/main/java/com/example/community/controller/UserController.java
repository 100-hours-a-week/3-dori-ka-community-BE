package com.example.community.controller;

import com.example.community.common.response.APIResponse;
import com.example.community.dto.request.user.UserSignUpDto;
import com.example.community.dto.response.user.SignUpResponse;
import com.example.community.dto.response.user.UserDetailResponse;
import com.example.community.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<APIResponse<SignUpResponse>> register(@Valid @RequestBody UserSignUpDto dto) {
        SignUpResponse registerMember = userService.signUp(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(APIResponse.success("회원가입 성공", registerMember));
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<UserDetailResponse>> getUserInfo(@PathVariable Long id) {
        UserDetailResponse userInfo = userService.getUserInfoById(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(APIResponse.success("조회 성공", userInfo));
    }

    @GetMapping("/email")
    public ResponseEntity<APIResponse<Boolean>> isEmailDuplicated(@RequestParam String email) {
        Boolean emailDuplicated = userService.isEmailDuplicated(email);
        return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success("이메일 중복 체크 성공", emailDuplicated));
    }


    @GetMapping("/nickname")
    public ResponseEntity<APIResponse<Boolean>> isNicknameDuplicated(@RequestParam String nickname) {
        Boolean nicknameDuplicated = userService.isNicknameDuplicated(nickname);
        return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success("이메일 중복 체크 성공", nicknameDuplicated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
