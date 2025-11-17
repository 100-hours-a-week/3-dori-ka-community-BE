package com.example.week7.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorMessage {

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"로그인 후 이용해주세요."),
    TOKEN_EXPIRE(HttpStatus.UNAUTHORIZED, "로그인이 시간이 만료됐습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN,  "권한이 없습니다."),
    INVALID_CREDENTIALS(HttpStatus.BAD_REQUEST, "이메일과 비밀번호를 확인해주세요."),

    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 접근입니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),

    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 페이지입니다."),

    EMAIL_DUPLICATED(HttpStatus.CONFLICT, "중복된 이메일입니다."),
    NICKNAME_DUPLICATED(HttpStatus.CONFLICT, "중복된 닉네임입니다."),
    DUPLICATED_LIKES(HttpStatus.CONFLICT, "이미 좋아요를 눌렀습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorMessage(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}